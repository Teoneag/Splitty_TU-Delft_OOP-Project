package client.scenes;

import client.services.*;
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
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;

import java.net.ConnectException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class AddExpenseCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ConfigService configService;
    private final ErrorService errorService;
    private final CurrencyService currencyService;
    private final I18NService i18NService;

    private Event event;
    private Expense expense;

    @FXML
    private Label addExpenseLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private TextField titleField;
    @FXML
    private Label amountLabel;
    @FXML
    private TextField amountField;
    @FXML
    private ChoiceBox<String> currencyChoiceBox;
    @FXML
    private Label dateLabel;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label sponsorLabel;
    @FXML
    private ChoiceBox<Participant> sponsorSelect;
    @FXML
    private CheckBox addAll;
    @FXML
    private CheckBox addOthers;
    @FXML
    private Label debtorsLabel;
    @FXML
    private CheckComboBox<Participant> debtorSelect;
    @FXML
    private Label tagLabel;
    @FXML
    private ChoiceBox<Tag> tagSelect;
    @FXML
    private Button cancelButton;
    @FXML
    private Button createButton;

    /**
     * Constructor
     *
     * @param server        serverUtils
     * @param mainCtrl      main controller
     * @param configService config service
     * @param errorService  error service
     */
    @Inject
    public AddExpenseCtrl(ServerUtils server, MainCtrl mainCtrl, ConfigService configService, ErrorService errorService, CurrencyService currencyService, I18NService i18NService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.configService = configService;
        this.errorService = errorService;
        this.currencyService = currencyService;
        this.i18NService = i18NService;
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
        setLanguage();

        currencyChoiceBox.getItems().addAll(currencyService.getCurrencies());

        final StringConverter<Participant> pConverter = AddPaymentCtrl.pConverter();
        sponsorSelect.setConverter(pConverter);
        debtorSelect.setConverter(pConverter);

        debtorSelect.getCheckModel().getCheckedItems().addListener((ListChangeListener<Participant>) c -> editChecks());

        StringConverter<Tag> tConverter = new StringConverter<>() {
            @Override
            public String toString(Tag t) {
                if (t == null) return "";
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
     */
    public void setLanguage() {
        i18NService.setTranslation(addExpenseLabel, "add.expense");
        i18NService.setTranslation(titleLabel, "title");
        i18NService.setTranslation(titleField, "expense.title");
        i18NService.setTranslation(amountLabel, "amount");
        i18NService.setTranslation(dateLabel, "date");
        i18NService.setTranslation(sponsorLabel, "sponsor");
        i18NService.setTranslation(addAll, "add.all");
        i18NService.setTranslation(addOthers, "add.all.others");
        i18NService.setTranslation(debtorsLabel, "debtors");
        i18NService.setTranslation(tagLabel, "tag");
        i18NService.setTranslation(cancelButton, "cancel");
        i18NService.setTranslation(createButton, "confirm");
    }

    /**
     * Refreshes all lists and sets all fields to empty
     *
     * @param event new event to create the event in
     */
    public void refresh(Event event) {
        this.event = event;
        expense = new Expense();
        titleField.clear();
        amountField.clear();
        currencyChoiceBox.setValue(configService.getConfigCurrency());
        datePicker.setValue(LocalDate.now());
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
        addExpenseLabel.textProperty().bind(i18NService.createStringBinding("expenseEdit"));
        this.event = event;
        this.expense = server.getExpense(expense.getId());
        expense = this.expense;
        titleField.setText(expense.getTitle());
        currencyChoiceBox.setValue(expense.getCurrency().getCurrencyCode());
        amountField.setText(Float.toString(expense.getAmount()));
        datePicker.setValue(expense.getDate());
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
            String newTitle = titleField.getText();
            if (newTitle.trim().isEmpty()) {
                // ToDo
                throw new IllegalArgumentException(i18NService.get("noTitle"));
            }
            expense.setTitle(newTitle);

            if (amountField.getText().isEmpty()) {
                // ToDo
//                throw new IllegalArgumentException(map.get("noAmount").toString());
            }
            float newAmount = Float.parseFloat(amountField.getText());
            if (newAmount < 0) {
                // ToDo
                throw new IllegalArgumentException(i18NService.get("positiveAmount"));
            }
            expense.setAmount(newAmount);

            // Don't know how to check if is valid date
            expense.setDate(datePicker.getValue());

            // Cannot give bad input - can only select from given options and cannot select empty
            expense.setCurrency(Currency.getInstance(currencyChoiceBox.getSelectionModel().getSelectedItem()));

            Participant newSponsor = sponsorSelect.getSelectionModel().getSelectedItem();
            if (newSponsor == null) {
                // ToDo
                throw new IllegalArgumentException(i18NService.get("noSponsor"));

            }
            expense.setSponsor(newSponsor);

            Set<Participant> newDebtors = new HashSet<>(debtorSelect.getCheckModel().getCheckedItems());
            if (newDebtors.isEmpty()) {
                // ToDo
                throw new IllegalArgumentException(i18NService.get("noDebtors"));
            }
            expense.setDebtors(newDebtors);

            Tag tag = tagSelect.getSelectionModel().getSelectedItem();
            if (tag == null) {
                // ToDo
                throw new IllegalArgumentException(i18NService.get("noTag"));
            }
            expense.setTag(tag);

            if (expense.getParentEvent() == null) {
                expense.setParentEvent(event);
                server.addExpense(expense);
            } else {
                server.updateExpense(expense);
            }
            mainCtrl.showEvent(event);
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
            mainCtrl.showEvent(event);
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
            // ToDo
//            sponsorSelect.setPromptText(map.get("sponsorsSelect").toString());
//            debtorSelect.setTitle(map.get("debtorsSelect").toString());
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
        if (expense.getId() <= 0) {
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
            addOthers.setSelected(debtorSelect.getCheckModel().getCheckedIndices().size() == debtorSelect.getItems().size() - 1 && !debtorSelect.getCheckModel().isChecked(sponsorSelect.getValue()));
    }

    /**
     * set visibility of addOthers box based on whether a sponsor is selected or not
     */
    @FXML
    private void sponsorChanged() {
        addOthers.setVisible(!(sponsorSelect.getSelectionModel().isEmpty() || sponsorSelect.getValue() == null));
    }

    /**
     * undo title to value from start of add/edit
     */
    public void undoTitle() {
        if (expense.getTitle() == null) titleField.clear();
        else titleField.setText(expense.getTitle());
    }

    /**
     * undo amount and currency to values from start of add/edit
     */
    public void undoAmountCurrency() {
        if (expense.getAmount() == 0) amountField.clear();
        else amountField.setText(Float.toString(expense.getAmount()));

        if (expense.getCurrency() == null) currencyChoiceBox.setValue(configService.getConfigCurrency());
        else currencyChoiceBox.setValue(expense.getCurrency().getCurrencyCode());

    }

    /**
     * undo date to value from start of add/edit
     */
    public void undoDate() {
        if (expense.getDate() == null) datePicker.setValue(LocalDate.now());
        else datePicker.setValue(expense.getDate());
    }

    /**
     * undo sponsor to value from start of add/edit
     */
    public void undoSponsor() {
        if (expense.getSponsor() == null) sponsorSelect.getSelectionModel().clearSelection();
        else sponsorSelect.setValue(expense.getSponsor());
    }

    /**
     * undo debtors to value from start of add/edit
     */
    public void undoDebtor() {
        debtorSelect.getCheckModel().clearChecks();
        if (expense.getDebtors() != null) for (Participant p : expense.getDebtors())
            debtorSelect.getCheckModel().check(p);

    }

    /**
     * undo tag to value from start of add/edit
     */
    public void undoTag() {
        if (expense.getTag() == null) tagSelect.getSelectionModel().clearSelection();
        else tagSelect.setValue(expense.getTag());
    }

}

















