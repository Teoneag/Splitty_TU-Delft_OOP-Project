package client.scenes;

import client.services.*;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import jakarta.ws.rs.ProcessingException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.awt.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class EventCtrl implements Initializable {

    private final ServerUtils server;
    private final StyleService style;
    private final MainCtrl mainCtrl;
    private final ErrorService errorService;
    private final I18NService i18NService;
    private final ConfigService configService;
    private final EventService eventService;
    private final StyleService styleService;

    private Event event;
    private ObservableList<Expense> backer;
    private Predicate<Expense> expensesPred;
    private Predicate<Expense> titlePred;
    private Predicate<Expense> sponsorPred;
    private Predicate<Expense> debtorPred;
    private Predicate<Expense> tagPred;

    @FXML
    private TextField titleTextField;
    @FXML
    private Label title;
    @FXML
    private Label titleErrorMessage;

    @FXML
    private TextField descriptionTextField;
    @FXML
    private Label description;
    @FXML
    private Label descriptionErrorMessage;

    @FXML
    private Label inviteCode;
    @FXML
    private Label copiedInviteCode;
    @FXML
    private Label createdDate;
    @FXML
    private Label lastModifiedDate;

    @FXML
    private Label emailSendLabel;
    @FXML
    private TableView<Participant> participantTable;
    @FXML
    private TableColumn<Participant, String> colParticipant;
    @FXML
    private Button addParticipantButton;
    @FXML
    private Button deleteParticipantButton;

    @FXML
    private Button tagsButton;
    @FXML
    private Button debtsButton;

    @FXML
    private TabPane transactionTables;
    @FXML
    private Tab expenseTab;
    @FXML
    private Tab paymentTab;
    @FXML
    private TableView<Expense> expenseTable;
    @FXML
    private TableColumn<Expense, String> expenseTitle;
    @FXML
    private TableColumn<Expense, String> expenseAmount;
    @FXML
    private TableColumn<Expense, String> expenseType;
    @FXML
    private TableView<Expense> paymentTable;
    @FXML
    private TableColumn<Expense, String> paymentSender;
    @FXML
    private TableColumn<Expense, String> paymentRecipient;
    @FXML
    private TableColumn<Expense, String> paymentAmount;
    @FXML
    private Button addExpenseButton;
    @FXML
    private Button deleteExpenseButton;

    @FXML
    private VBox filterPane;
    @FXML
    private Label filterLabel;
    @FXML
    private TextField filterTitle;
    @FXML
    private Label sponsorLabel;
    @FXML
    private ChoiceBox<Participant> filterSponsor;
    @FXML
    private Label debtorLabel;
    @FXML
    private ChoiceBox<Participant> filterDebtor;
    @FXML
    private Label labelLabel;
    @FXML
    private ChoiceBox<Tag> filterTag;
    @FXML
    private Button clearFiltersButton;

    /**
     * Constructor
     *
     * @param server        the server utils
     * @param style         the style utils
     * @param mainCtrl      the main controller
     * @param i18NService   the language service
     * @param configService the config service
     * @param eventService  the event service
     * @param errorService  the error service
     */
    @Inject
    public EventCtrl(ServerUtils server, StyleService style, MainCtrl mainCtrl, I18NService i18NService,
                     ConfigService configService, EventService eventService, ErrorService errorService,
                     StyleService styleService) {
        this.server = server;
        this.style = style;
        this.mainCtrl = mainCtrl;
        this.i18NService = i18NService;
        this.configService = configService;
        this.eventService = eventService;
        this.errorService = errorService;
        this.styleService = styleService;
    }

    /**
     * initialize
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setLanguage();

        expenseTable.setOnMouseClicked(expenseOnClick());
        paymentTable.setOnMouseClicked(paymentOnClick());
        participantTable.setOnMouseClicked(participantOnClick());

        transactionTables.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        expenseTab.selectedProperty().addListener((observable, oldValue, newValue) -> filterPane.setVisible(newValue));

        backer = expenseTable.getItems();

        updatePred();
        expenseTable.setItems(new FilteredList<>(backer, expensesPred));

        paymentTable.setItems(new FilteredList<>(backer, e -> e.getTag().getName().equals("Payment")));

        expenseTitle.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getTitle()));
        expenseAmount.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().formattedAmount()));
        expenseType.setCellValueFactory(e -> new ReadOnlyObjectWrapper<>(e.getValue().getTag() != null ? e.getValue().getTag().getName() : ""));
        expenseType.setCellFactory(tc -> new TableCell<>() {
            {
                itemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        setText(null);
                        setStyle(null);
                    } else {
                        setText(getItem());
                        Color c = new Color(getTableRow().getItem().getTag().getColor());
                        setStyle(cString(c));
                    }
                });
            }
        });

        paymentSender.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getSponsor().getFullName()));
        paymentRecipient.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getDebtors().stream().findFirst().orElseThrow().getFullName()));
        paymentAmount.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().formattedAmount()));

        colParticipant.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getFullName()));

        titleTextField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ESCAPE) {
                cancelEditTitle();
            }
        });

        descriptionTextField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ESCAPE) {
                cancelEditDescription();
            }
        });

        setFilterControls();
    }

    /**
     * Set the language of the user
     */
    public void setLanguage() {
        i18NService.setTranslation(copiedInviteCode, "invite.code.copied");
        i18NService.setTranslation(emailSendLabel, "email.send.to", configService.getEmail());
        i18NService.setTranslation(participantTable, "table.noContent");
        i18NService.setTranslation(colParticipant, "participants");
        i18NService.setTranslation(addParticipantButton, "add");
        i18NService.setTranslation(deleteParticipantButton, "delete");
        i18NService.setTranslation(tagsButton, "statistics");
        i18NService.setTranslation(debtsButton, "debts");
        i18NService.setTranslation(expenseTab, "expenses");
        i18NService.setTranslation(paymentTab, "payments");
        i18NService.setTranslation(expenseTable, "table.noContent");
        i18NService.setTranslation(expenseTitle, "title");
        i18NService.setTranslation(expenseAmount, "amount");
        i18NService.setTranslation(expenseType, "type");
        i18NService.setTranslation(paymentTable, "table.noContent");
        i18NService.setTranslation(paymentSender, "sponsor");
        i18NService.setTranslation(paymentRecipient, "recipient");
        i18NService.setTranslation(paymentAmount, "amount");
        i18NService.setTranslation(addExpenseButton, "add");
        i18NService.setTranslation(deleteExpenseButton, "delete");
        i18NService.setTranslation(filterLabel, "filter.expenses");
        i18NService.setTranslation(filterTitle, "expense.title");
        i18NService.setTranslation(sponsorLabel, "sponsor");
        i18NService.setTranslation(debtorLabel, "debtor");
        i18NService.setTranslation(labelLabel, "label");
        i18NService.setTranslation(clearFiltersButton, "clear.filters");
    }

    public static String cString(Color c) {
        String textColor = c.getRed() * 0.299 + c.getGreen() * 0.587 + c.getBlue() * 0.114 > 145 ? "black" : "white";
        return String.format("-fx-background-color: rgba(%d,%d,%d,1); " + "-fx-background-radius: 5px; " + "-fx-text-fill: %s", c.getRed(), c.getGreen(), c.getBlue(), textColor);
    }

//    /**
//     * Refreshes the event overview with the given invite code.
//     *
//     * @param inviteCode the invite code of the event used as uid
//     */
//    public void refresh(String inviteCode) {
//        try {
//            event = server.getEvent(inviteCode);
//            refresh(event);
//
//            if (!connectWebsocket()) {
//                mainCtrl.serverConnectionAlert();
//                return;
//            }
//            server.registerForEvents(inviteCode, this::refreshWs);
//            server.registerForParticipants(inviteCode, this::updateParticipants);
//            server.registerForExpenses(inviteCode, this::updateExpenses);
//        } catch (ProcessingException e) {
//            if (e.getCause().getClass() == ConnectException.class) {
//                mainCtrl.serverConnectionAlert();
//            }
//        }
//    }


    /**
     * Refreshes the event overview with the given event.
     *
     * @param event the event to refresh with
     */
    public void refresh(Event event) {
        this.event = event;
        title.setText(event.getTitle());
        description.setText(event.getDescription());
        this.inviteCode.setText(String.valueOf(event.getInviteCode()));
        i18NService.setTranslation(createdDate, "created", event.getCreationDate());
        i18NService.setTranslation(lastModifiedDate, "last.modified", event.getLastModified());
        refreshExpenses();
        refreshParticipants();
        refreshTags();
        updatePred();
        transactionTables.getSelectionModel().select(expenseTab);
        style.computeTableInsets(expenseTable);
        style.computeTableInsets(paymentTable);
        style.computeTableInsets(participantTable);
    }

    /**
     * Updates the last modified date of the event.
     */
    public void updateLastModified() {
        try {
            event.onUpdate();
            server.updateEvent(event);
//            lastUpdatedDate.setText(getLastModifiedString()); ToDo
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }
    }

    /**
     * Refresh the table
     */
    public void refreshParticipants() {
        try {
            List<Participant> participants = server.getParticipantsByEventInviteCode(event.getInviteCode());
            participantTable.getItems().clear();
            participantTable.getItems().addAll(participants);
            participantTable.refresh();

            filterSponsor.getItems().clear();
            filterSponsor.getItems().addAll(participants);

            filterDebtor.getItems().clear();
            filterDebtor.getItems().addAll(participants);
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }
    }

    /**
     * Updates the participants table with the refreshed participants list
     *
     * @param participants the list of participants to update the table with
     */
    public void updateParticipants(List<Participant> participants) {
        Platform.runLater(() -> {
            participantTable.getItems().clear();
            participantTable.getItems().addAll(participants);
            participantTable.refresh();

            filterSponsor.getItems().clear();
            filterSponsor.getItems().addAll(participants);

            filterDebtor.getItems().clear();
            filterDebtor.getItems().addAll(participants);
        });
    }

    /**
     * Updates expense table with the refreshed expenses list;
     */
    public void refreshExpenses() {
        try {
            List<Expense> expenses = server.getTransactionsByCurrency(event.getInviteCode());
            backer.clear();
            backer.addAll(expenses);
            expenseTable.refresh();
            paymentTable.refresh();
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }
    }

    /**
     * Updates the expenses table with the refreshed expenses list
     *
     * @param expenses the list of expenses to update the table with
     */
    public void updateExpenses(List<Expense> expenses) {
        List<Expense> newExpenses = server.getTransactionsByCurrency(event.getInviteCode());
        Platform.runLater(() -> {
            backer.clear();
            backer.addAll(newExpenses);
            expenseTable.refresh();
            paymentTable.refresh();
        });
    }

    /**
     * refreshes the tag dropdown
     */
    public void refreshTags() {
        try {
            List<Tag> tags = server.getTagsByEvent(event.getInviteCode());
            filterTag.getItems().clear();
            filterTag.getItems().addAll(tags);
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }
    }

    /**
     * Refreshes the event overview with the given event from the websocket.
     *
     * @param event the event to refresh with
     */
    public void refreshWs(Event event) {
        this.event = event;
        Platform.runLater(() -> { // Run on JavaFX thread
            title.setText(event.getTitle());
            description.setText(event.getDescription());
        });

        this.inviteCode.setText(String.valueOf(event.getInviteCode()));
//        createdDate.setText(getCreatedString()); ToDo
//        lastUpdatedDate.setText(getLastModifiedString());
    }

    @FXML
    public void addParticipant() {
        mainCtrl.showAddParticipant(event);
        updateLastModified();
    }

    /**
     * Deletes the currently selected participant from the participant table
     */
    public void deleteParticipant() {
        try {
            if (participantTable.getSelectionModel().isEmpty()) throw new IllegalArgumentException();
            int i = participantTable.getSelectionModel().getFocusedIndex();
            Participant participant = participantTable.getItems().get(i);
            Alert alert = errorService.confirmDeleteParticipant(participant);
            alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
                server.deleteParticipant(participant);
                updateLastModified();
                refresh(event);
            });
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        } catch (IllegalArgumentException e) {
            Alert alert = errorService.cannotDelete("delete_participant", "no_participant_to_delete");
            alert.show();
        }
    }

    /**
     * onAction for addExpense button
     */
    @FXML
    public void addExpense() {
        if (paymentTab.isSelected()) {
            mainCtrl.showAddPayment(event, true);
            updateLastModified();
        } else {
            mainCtrl.showAddExpense(event);
            updateLastModified();
        }
    }

    /**
     * deletes the currently selected expense in the table
     */
    @FXML
    public void deleteExpense() {
        TableView<Expense> table;
        boolean isPayment = paymentTab.isSelected();
        if (isPayment) {
            table = paymentTable;
        } else {
            table = expenseTable;
        }
        try {
            if (table.getSelectionModel().isEmpty()) {
                throw new IllegalArgumentException();
            }
            int i = table.getSelectionModel().getFocusedIndex();
            Expense expense = table.getItems().get(i);
            Alert alert = errorService.confirmDelete(isPayment, expense);
            alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
                server.deleteExpense(expense);
                updateLastModified();
                refresh(event);
            });
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        } catch (IllegalArgumentException e) {
            Alert alert = errorService.cannotDelete("delete_expense", "no_expenses_to_delete");
            alert.show();
        }
    }

    /**
     * Shows the text field for the title and hides the label.
     * Limits the length of the title to 25 characters.
     */
    @FXML
    public void titleShowTextField() {
        titleTextField.setText(title.getText());
        titleTextField.setVisible(true);
        title.setVisible(false);
        titleTextField.requestFocus();
        errorService.bindTitleCheck(titleTextField, titleErrorMessage);
        modifyTextFieldLength(titleTextField);
    }

    /**
     * Shows the label for the title and hides the text field.
     */
    @FXML
    public void titleShowLabel() {
        if (titleTextField.getText().length() < 3) {
            if (titleTextField.getText().isEmpty()) {
                titleTextField.setText(configService.getLanguage().get("title").toString());
            } else {
                titleTextField.setText(titleTextField.getText() + configService.getLanguage().get("is_too_short").toString());
            }
        }
        title.setText(titleTextField.getText());
        titleTextField.setVisible(false);
        title.setVisible(true);
        event.setTitle(titleTextField.getText());
        updateLastModified();
    }

    @FXML
    public void copyInviteCode() {
        styleService.copyInviteCode(copiedInviteCode, inviteCode.getText());
    }

    public void showEmailSent(String email) {
        emailSendLabel.setText(configService.getLanguage().get("emailSentTo").toString() + email);
        styleService.playFadeTransition(emailSendLabel);
    }

    /**
     * Shows the text field for the description and hides the label.
     */
    @FXML
    public void descriptionShowTextField() {
        descriptionTextField.setText(description.getText());
        descriptionTextField.setVisible(true);
        description.setVisible(false);
        descriptionTextField.requestFocus();

        errorService.bindDescriptionCheck(descriptionTextField, descriptionErrorMessage);
        modifyTextFieldLength(descriptionTextField);
    }

    /**
     * Shows the label for the description and hides the text field.
     */
    @FXML
    public void descriptionShowLabel() {
        if (descriptionTextField.getText().length() < 3) {
            if (descriptionTextField.getText().isEmpty()) {
                descriptionTextField.setText(configService.getLanguage().get("desc").toString());
            } else {
                descriptionTextField.setText(descriptionTextField.getText() + configService.getLanguage().get("is_too_short").toString());
            }
        }
        description.setText(descriptionTextField.getText());
        descriptionTextField.setVisible(false);
        description.setVisible(true);
        event.setDescription(descriptionTextField.getText());
        updateLastModified();
    }

    @FXML
    public void cancelEditTitle() {
        titleTextField.setVisible(false);
        title.setVisible(true);
    }

    @FXML
    public void cancelEditDescription() {
        descriptionTextField.setVisible(false);
        description.setVisible(true);
    }

    /**
     * Returns an event handler showing the edit participant view
     * when the participant is clicked on twice
     *
     * @return the event handler
     */
    @FXML
    public EventHandler<MouseEvent> participantOnClick() {
        return e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Participant selected = participantTable.getSelectionModel().getSelectedItem();
                mainCtrl.showEditParticipant(this.event, selected);
                updateLastModified();
            }
        };
    }

    /**
     * Returns an event handler which shows the edit expense overview
     * for an event when it is double-clicked in the table
     *
     * @return event handler lambda
     */
    @FXML
    public EventHandler<MouseEvent> expenseOnClick() {
        return e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Expense selected = expenseTable.getSelectionModel().getSelectedItem();
                mainCtrl.showEditExpense(this.event, selected);
                updateLastModified();
            }
        };
    }

    /**
     * Returns an event handler which shows the edit expense overview
     * for an event when it is double-clicked in the table
     *
     * @return event handler lambda
     */
    @FXML
    public EventHandler<MouseEvent> paymentOnClick() {
        return e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Expense selected = paymentTable.getSelectionModel().getSelectedItem();
                mainCtrl.showEditPayment(this.event, selected);
                updateLastModified();
            }
        };
    }

    /**
     * Modifies the width of the text field based on the length of the text.
     *
     * @param textField the text field to modify
     */
    private void modifyTextFieldLength(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            double baseWidth = 20;
            double widthPerCharacter = 7;
            double newWidth = baseWidth + newValue.length() * widthPerCharacter;
            textField.setPrefWidth(newWidth);
        });
    }

    /**
     * Attempts to create a connection to the websocket
     *
     * @return true if the server is connected to websockets, false otherwise
     */
    public Boolean connectWebsocket() {
        String serverString = configService.getServer().replace("https:", "").replace("http:", "");
        server.connect("ws:" + serverString + "websocket", this::webSocketFailure);
        return server.isConnected();
    }

    /**
     * Triggered when the websocket connection changes to connected or when it fails
     *
     * @param isWorking true if the websocket is connected, false otherwise
     */
    public void webSocketFailure(Boolean isWorking) {
        if (!isWorking) {
            // ToDo show WebSocket connection failed
            Platform.runLater(mainCtrl::serverConnectionAlert);
        }
    }

    public void showDebts() {
        mainCtrl.showDebtOverview(event);
    }

    public void manageTags() {
        mainCtrl.showManageTags(event);
    }

    /**
     * initialise the onAction for the filters
     */
    public void setFilterControls() {
        StringConverter<Participant> pConverter = new StringConverter<>() {
            @Override
            public String toString(Participant participant) {
                if (participant == null) return "";
                return participant.getFullName();
            }

            @Override
            public Participant fromString(String string) {
                return null;
            }
        };
        filterSponsor.setConverter(pConverter);
        filterDebtor.setConverter(pConverter);
        filterTag.setConverter(new StringConverter<>() {
            @Override
            public String toString(Tag tag) {
                if (tag == null) return "";
                return tag.getName();
            }

            @Override
            public Tag fromString(String string) {
                return null;
            }
        });
    }

    /**
     * updates the predicate for the expense table
     */
    public void updatePred() {
        expensesPred = e -> !e.getTag().getName().equals("Payment");
        expensesPred = expensesPred.and(titlePred == null ? e -> true : titlePred).and(sponsorPred == null ? e -> true : sponsorPred).and(debtorPred == null ? e -> true : debtorPred).and(tagPred == null ? e -> true : tagPred);
        expenseTable.setItems(new FilteredList<>(backer, expensesPred));
    }

    /**
     * onAction for the title filter
     */
    public void titleFilterOnAction() {
        if (filterTitle.getText() == null || filterTitle.getText().isEmpty()) titlePred = e -> true;
        else titlePred = ex -> ex.getTitle().contains(filterTitle.getText());
        updatePred();
    }

    /**
     * onAction for the sponsor filter
     */
    public void sponsorFilterOnAction() {
        if (filterSponsor.getValue() == null) sponsorPred = e -> true;
        else sponsorPred = e -> e.getSponsor().equals(filterSponsor.getValue());
        updatePred();
    }

    /**
     * onAction for the debtor filter
     */
    public void debtorFilterOnAction() {
        if (filterDebtor.getValue() == null) debtorPred = e -> true;
        else debtorPred = e -> e.getDebtors().contains(filterDebtor.getValue());
        updatePred();
    }

    /**
     * onAction for the tag filter
     */
    public void tagFilterOnAction() {
        if (filterTag.getValue() == null) tagPred = e -> true;
        else tagPred = e -> e.getTag().equals(filterTag.getValue());
        updatePred();
    }

    /**
     * clears the filters
     */
    public void clearFilters() {
        filterTitle.clear();
        filterSponsor.getSelectionModel().clearSelection();
        filterDebtor.getSelectionModel().clearSelection();
        filterTag.getSelectionModel().clearSelection();

        updatePred();
    }
}
