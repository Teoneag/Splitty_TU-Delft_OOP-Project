package client.scenes;

import client.services.ConfigService;
import client.services.ServerUtils;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import jakarta.ws.rs.ProcessingException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.util.StringConverter;

import javax.inject.Inject;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class AddPaymentCtrl implements Initializable{
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ConfigService configService;

    private Event event;
    private Expense expense;
    private Tag paymentTag;
    private boolean fromEvent;

    @FXML
    private TextField amount;
    @FXML
    private ComboBox<String> currency;
    @FXML
    private DatePicker date;
    @FXML
    private ComboBox<Participant> sponsorSelect;
    @FXML
    private ComboBox<Participant> debtorSelect;

    @FXML
    private Text addPaymentHead;
    @FXML
    private Text expenseAmount;
    @FXML
    private Text expenseDate;
    @FXML
    private Text expenseSponsor;
    @FXML
    private Text expenseDebtors;
    @FXML
    private Button createExpenseButton;
    @FXML
    private Button cancelButton;

    /**
     * Initialize
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currency.getItems().addAll("EUR", "USD", "CHF", "GBP");
        currency.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue == null || oldValue.equals(newValue)) return;
            float rate = server.getRate(newValue, oldValue, expense.getDate().toString());
            float newAmount = Float.parseFloat(amount.getText()) / rate;
            amount.setText(String.valueOf(newAmount));
        });

        StringConverter<Participant> pConverter = new StringConverter<>() {
            @Override
            public String toString(Participant p) {
                return p.getFirstName() + " " + p.getLastName();
            }

            @Override
            public Participant fromString(String string) {
                return null;
            }
        };
        sponsorSelect.setConverter(pConverter);
        debtorSelect.setConverter(pConverter);
    }

    /**
     * Set the language of the page
     *
     * @param map the language map which contains the translation
     * @throws IOException if the language file is not found
     */
    public void setLanguage(HashMap<String, Object> map) {
        addPaymentHead.setText((String) map.get("paymentTitle"));
        expenseAmount.setText((String) map.get("expenseAmount"));
        expenseDate.setText((String) map.get("expenseDate"));
        expenseSponsor.setText((String) map.get("expenseSponsor"));
        expenseDebtors.setText((String) map.get("recipient"));
        debtorSelect.setPromptText((String) map.get("recipientSelect"));
        sponsorSelect.setPromptText((String) map.get("sponsorSelect"));

        createExpenseButton.setText((String) map.get("confirmExpenseButton"));
        cancelButton.setText((String) map.get("cancelButton"));

        // Set button sizes based on text length
//        mainCtrl.setDynamicButtonSize(createExpenseButton);
//        mainCtrl.setDynamicButtonSize(cancelButton);
    }

    public void goBack() {
        mainCtrl.showEventOverview(event);
    }

    /**
     * Constructor
     *
     * @param server            serverUtils
     * @param mainCtrl          main controller
     * @param configService     configService
     */
    @Inject
    public AddPaymentCtrl(ServerUtils server, MainCtrl mainCtrl,
                          ConfigService configService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.configService = configService;
    }

    /**
     * Refreshes the page with the new event, and updates the list of participants from the server
     *
     * @param event new event to create the event in
     * @param fromEvent is this from the event overview?
     */
    public void refresh(Event event, boolean fromEvent) {
        this.fromEvent = fromEvent;
        this.event = event;
        paymentTag = server.getPaymentTag(event);
        expense = new Expense();
        amount.clear();
        currency.setValue(configService.getConfigCurrency());
        date.setValue(LocalDate.now());
        sponsorSelect.getSelectionModel().clearSelection();
        debtorSelect.getSelectionModel().clearSelection();
        repopulateDropdowns();
    }

    /**
     * @param event   parent event
     * @param expense expense being edited
     */
    public void edit(Event event, Expense expense) {
        fromEvent = true;
        this.event = event;
        this.paymentTag = server.getPaymentTag(event);
        this.expense = server.getExpense(expense.getId());
        expense = this.expense;
        currency.setValue(expense.getCurrency().getCurrencyCode());
        amount.setText(Float.toString(expense.getAmount()));
        date.setValue(expense.getDate());
        repopulateDropdowns();
        sponsorSelect.getSelectionModel().select(expense.getSponsor());
        debtorSelect.getSelectionModel().select(expense.getDebtors().stream().findFirst().orElseThrow());
    }

    /**
     * Triggered once "create" button is clicked
     * Returns to the event overview after posting the new expense
     */
    public void createExpense() {
        try {
            float newAmount = Float.parseFloat(amount.getText());
            if (newAmount < 0) throw new IllegalArgumentException("Please enter a positive amount");
            expense.setAmount(newAmount);

            // Don't know how to check if is valid date
            expense.setDate(date.getValue());

            // Cannot give bad input - can only select from given options and cannot select empty
            expense.setCurrency(Currency.getInstance(currency.getSelectionModel().getSelectedItem()));

            Participant newSponsor = sponsorSelect.getSelectionModel().getSelectedItem();
            if (newSponsor == null) throw new IllegalArgumentException("Please select a sender");
            expense.setSponsor(newSponsor);

            Participant newDebtor = debtorSelect.getSelectionModel().getSelectedItem();
            if (newDebtor == null) throw new IllegalArgumentException("Please select a recipient");
            Set<Participant> newDebtors = new HashSet<>();
            newDebtors.add(newDebtor);
            expense.setDebtors(newDebtors);

            expense.setTitle(expense.formattedAmount() +
                    " from " + expense.getSponsor().getFirstName() +
                    " to " + expense.getDebtors().stream().findFirst().orElseThrow().getFirstName() +
                    " on " + expense.getDate().toString()
            );

            expense.setTag(paymentTag);

            if (expense.getParentEvent() == null) {
                expense.setParentEvent(event);
                server.addExpense(expense);
            } else {
                server.updateExpense(expense);
            }

            if (fromEvent)
                mainCtrl.showEventOverview(event);
            else
                mainCtrl.showDebtOverview(event);

        } catch (NumberFormatException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText("Please enter a numerical amount");
            alert.showAndWait();
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        } catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            throw e;
        }
    }

    /**
     * Triggered once "cancel" button is clicked
     * Returns to the event overview
     */
    public void cancel() {
        try {
            if (fromEvent)
                mainCtrl.showEventOverview(event);
            else
                mainCtrl.showDebtOverview(event);
        } catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Remove all participants from both the sponsor and debtor dropdown boxes
     * and repopulate them with the current participants set.
     */
    public void repopulateDropdowns() {
        try {
            List<Participant> participants = server.getParticipantsByEventInviteCode(event.getInviteCode());
            sponsorSelect.getItems().removeAll(sponsorSelect.getItems());
            debtorSelect.getItems().removeAll(debtorSelect.getItems());
            sponsorSelect.getItems().addAll(participants);
            debtorSelect.getItems().addAll(participants);
            sponsorSelect.setPromptText("Select Sponsor");
            debtorSelect.setPromptText("Select Debtors");
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }
    }

}
