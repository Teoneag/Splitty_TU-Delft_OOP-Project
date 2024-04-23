package client.scenes;

import client.services.ConfigService;
import client.services.ErrorService;
import client.services.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import jakarta.ws.rs.ProcessingException;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.springframework.stereotype.Controller;

import java.net.ConnectException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

@Controller
public class AddExpenseCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ConfigService configService;
    private final ErrorService errorService;

    private Event event;
    private Expense expense;

    @FXML
    private TextField title;
    @FXML
    private TextField amount;
    @FXML
    private ComboBox<String> currency;
    @FXML
    private DatePicker date;
    @FXML
    private ComboBox<Participant> sponsorSelect;
    @FXML
    private CheckComboBox<Participant> debtorSelect;
    @FXML
    private ComboBox<Tag> tagSelect;
    @FXML
    private Text addExpenseHead;
    @FXML
    private Text expenseTitle;
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
    @FXML
    private CheckBox addAll;
    @FXML
    private CheckBox addOthers;
    private HashMap<String, Object> map;

    /**
     * Constructor
     *
     * @param server            serverUtils
     * @param mainCtrl          main controller
     * @param configService     config service
     * @param errorService      error service
     */
    @Inject
    public AddExpenseCtrl(ServerUtils server, MainCtrl mainCtrl,
                          ConfigService configService, ErrorService errorService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.configService = configService;
        this.errorService = errorService;
    }

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
            if (oldValue == null || newValue == null || oldValue.equals(newValue) || amount.getText().isEmpty()) return;
            float rate = server.getRate(newValue, oldValue, date.getValue().toString());
            float newAmount = Float.parseFloat(amount.getText()) / rate;
            amount.setText(String.valueOf(newAmount));
        });

        final StringConverter<Participant> pConverter = AddPaymentCtrl.pConverter();
        sponsorSelect.setConverter(pConverter);
        debtorSelect.setConverter(pConverter);

        debtorSelect.getCheckModel().getCheckedItems().addListener((ListChangeListener<Participant>) c -> editChecks());

        StringConverter<Tag> tConverter = new StringConverter<>() {
            @Override
            public String toString(Tag t) {
                return t.getName();
            }

            @Override
            public Tag fromString(String string) {
                return null;
            }
        };
        tagSelect.setConverter(tConverter);
    }

    /**
     * Set the language of the page
     *
     * @param map the language map which contains the translation
     */
    public void setLanguage(HashMap<String, Object> map) {
        expenseTitle.setText((String) map.get("expenseTitle"));
        expenseAmount.setText((String) map.get("expenseAmount"));
        expenseDate.setText((String) map.get("expenseDate"));
        expenseSponsor.setText((String) map.get("expenseSponsor"));
        expenseDebtors.setText((String) map.get("expenseDebtors"));
        title.setPromptText((String) map.get("titlePrompt"));
        debtorSelect.setTitle((String) map.get("debtorsSelect"));
        sponsorSelect.setPromptText((String) map.get("sponsorsSelect"));

        this.map = map;
        errorService.changeLanguage(map);

        createExpenseButton.setText((String) map.get("confirmExpenseButton"));
        cancelButton.setText((String) map.get("cancelButton"));

        // Set button sizes based on text length
        mainCtrl.setDynamicButtonSize(createExpenseButton);
        mainCtrl.setDynamicButtonSize(cancelButton);
    }

    public void goBack() {
        mainCtrl.showEventOverview(event);
    }

    /**
     * Refreshes all lists and sets all fields to empty
     *
     * @param event new event to create the event in
     */
    public void refresh(Event event) {
        addExpenseHead.setText(map.get("expenseCreate").toString());
        this.event = event;
        expense = new Expense();
        title.clear();
        amount.clear();
        currency.setValue(configService.getConfigCurrency());
        date.setValue(LocalDate.now());
        sponsorSelect.getSelectionModel().clearSelection();
        debtorSelect.getCheckModel().clearChecks();
        addAll.setSelected(false);
        addOthers.setSelected(false);
        repopulateDropdowns();
        repopulateTagDropdown(event);
        tagSelect.getSelectionModel().clearSelection();
        sponsorChanged();
    }

    /**
     * Refreshes all lists and sets all fields to those of expense
     *
     * @param event   parent event
     * @param expense expense being edited
     */
    public void edit(Event event, Expense expense) {
        addExpenseHead.setText(map.get("expenseEdit").toString());
        this.event = event;
        this.expense = server.getExpense(expense.getId());
        expense = this.expense;
        title.setText(expense.getTitle());
        currency.setValue(expense.getCurrency().getCurrencyCode());
        amount.setText(Float.toString(expense.getAmount()));
        date.setValue(expense.getDate());
        repopulateDropdowns();
        sponsorSelect.getSelectionModel().select(expense.getSponsor());
        debtorSelect.getCheckModel().clearChecks();
        for (Participant p : expense.getDebtors()) {
            debtorSelect.getCheckModel().check(p);
        }
        repopulateTagDropdown(event);
        tagSelect.getSelectionModel().select(expense.getTag());
        sponsorChanged();
    }

    /**
     * Refreshes tag list and selects new tag
     *
     * @param tag new tag
     */
    public void reducedRefresh(Tag tag) {
        event = server.getEvent(event.getInviteCode());
        repopulateTagDropdown(event);
        tagSelect.getSelectionModel().select(tag);
    }

    /**
     * Triggered once "create" button is clicked
     * Returns to the event overview after posting the new expense
     */
    public void createExpense() {
        try {
            String newTitle = title.getText();
            if (newTitle.trim().isEmpty()) throw new IllegalArgumentException(map.get("noTitle").toString());
            expense.setTitle(newTitle);

            if (amount.getText().isEmpty()) throw new IllegalArgumentException(map.get("noAmount").toString());
            float newAmount = Float.parseFloat(amount.getText());
            if (newAmount < 0) throw new IllegalArgumentException(map.get("positiveAmount").toString());
            expense.setAmount(newAmount);

            // Don't know how to check if is valid date
            expense.setDate(date.getValue());

            // Cannot give bad input - can only select from given options and cannot select empty
            expense.setCurrency(Currency.getInstance(currency.getSelectionModel().getSelectedItem()));

            Participant newSponsor = sponsorSelect.getSelectionModel().getSelectedItem();
            if (newSponsor == null) throw new IllegalArgumentException(map.get("noSponsor").toString());
            expense.setSponsor(newSponsor);

            Set<Participant> newDebtors = new HashSet<>(debtorSelect.getCheckModel().getCheckedItems());
            if (newDebtors.isEmpty()) throw new IllegalArgumentException(map.get("noDebtors").toString());
            expense.setDebtors(newDebtors);

            Tag tag = tagSelect.getSelectionModel().getSelectedItem();
            if (tag == null) {
                throw new IllegalArgumentException(map.get("noTag").toString());
            }
            expense.setTag(tag);

            if (expense.getParentEvent() == null) {
                expense.setParentEvent(event);
                server.addExpense(expense);
            } else {
                server.updateExpense(expense);
            }
            mainCtrl.showEventOverview(event);
        } catch (NumberFormatException e) {
            Alert alert = errorService.numberFormatError();
            alert.showAndWait();
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        } catch (Exception e) {
            Alert alert = errorService.generalError(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Triggered once "cancel" button is clicked
     * Returns to the event overview
     */
    public void cancel() {
        try {
            mainCtrl.showEventOverview(event);
        } catch (Exception e) {
            Alert alert = errorService.generalError(e.getMessage());
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
            sponsorSelect.getItems().clear();
            debtorSelect.getItems().clear();
            sponsorSelect.getItems().addAll(participants);
            debtorSelect.getItems().addAll(participants);
            sponsorSelect.setPromptText(map.get("sponsorsSelect").toString());
            debtorSelect.setTitle(map.get("debtorsSelect").toString());
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }
    }

    /**
     * Repopulate the tag dropdown with all tags
     *
     * @param event takes the event to get the tags from
     */
//    TODO change getAllTags to get tags by event but for some reason it doesnt work yet.
    public void repopulateTagDropdown(Event event) {
        List<Tag> tags = server.getTagsByEvent(event.getInviteCode());
        tags.removeIf(t -> t.getName().equals("Payment"));
        tagSelect.getItems().clear();
        tagSelect.getItems().addAll(tags);
    }

    /**
     * Create a new tag and show the add tag screen
     */
    public void createNewTag() {
        if(expense.getId() <= 0){
            mainCtrl.showAddTagNoExpense(event);
        } else {
            mainCtrl.showAddTag(event, server.getExpense(expense.getId()));
        }
    }

    /**
     * onAction for addAll
     * - checks all debtors if checked, else unchecks all
     * - addOthers is automatically unchecked
     */
    public void addAll() {
        if (addAll.isSelected()) debtorSelect.getCheckModel().checkAll();
        else debtorSelect.getCheckModel().clearChecks();
        addOthers.setSelected(false);
    }

    /**
     * onAction for addOthers
     * - checks all debtors except the sponsor if checked, else unchecks all
     * - addAll is automatically unchecked
     */
    public void addOthers() {
        if (addOthers.isSelected()) {
            debtorSelect.getCheckModel().checkAll();
            debtorSelect.getCheckModel().clearCheck(sponsorSelect.getValue());
        } else {
            debtorSelect.getCheckModel().clearChecks();
        }
        addAll.setSelected(false);
    }

    /**
     * ListChangeListener for when the check combo box changes, to set values of addAll and addOthers checkboxes
     */
    public void editChecks() {
        addAll.setSelected(debtorSelect.getCheckModel().getCheckedIndices().size() == debtorSelect.getItems().size());
        if (!sponsorSelect.getSelectionModel().isEmpty())
            addOthers.setSelected(debtorSelect.getCheckModel().getCheckedIndices().size()
                    == debtorSelect.getItems().size() - 1
                    && !debtorSelect.getCheckModel().isChecked(sponsorSelect.getValue()));
    }

    /**
     * set visibility of addOthers box based on whether a sponsor is selected or not
     */
    public void sponsorChanged() {
        addOthers.setVisible(!(sponsorSelect.getSelectionModel().isEmpty() || sponsorSelect.getValue() == null));
    }

    /**
     * undo title to value from start of add/edit
     */
    public void undoTitle() {
        if (expense.getTitle() == null)
            title.clear();
        else
            title.setText(expense.getTitle());
    }

    /**
     * undo amount and currency to values from start of add/edit
     */
    public void undoAmountCurrency() {
        if (expense.getAmount() == 0)
            amount.clear();
        else
            amount.setText(Float.toString(expense.getAmount()));

        if (expense.getCurrency() == null)
            currency.setValue(configService.getConfigCurrency());
        else
            currency.setValue(expense.getCurrency().getCurrencyCode());

    }

    /**
     * undo date to value from start of add/edit
     */
    public void undoDate() {
        if (expense.getDate() == null)
            date.setValue(LocalDate.now());
        else
            date.setValue(expense.getDate());
    }

    /**
     * undo sponsor to value from start of add/edit
     */
    public void undoSponsor() {
        if (expense.getSponsor() == null)
            sponsorSelect.getSelectionModel().clearSelection();
        else
            sponsorSelect.setValue(expense.getSponsor());
    }

    /**
     * undo debtors to value from start of add/edit
     */
    public void undoDebtor() {
        debtorSelect.getCheckModel().clearChecks();
        if (expense.getDebtors() != null)
            for(Participant p : expense.getDebtors())
                debtorSelect.getCheckModel().check(p);

    }

    /**
     * undo tag to value from start of add/edit
     */
    public void undoTag() {
        if (expense.getTag() == null)
            tagSelect.getSelectionModel().clearSelection();
        else
            tagSelect.setValue(expense.getTag());
    }

}

















