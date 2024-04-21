package client.scenes;

import client.services.DebtUtils;
import client.services.ErrorService;
import client.services.ServerUtils;
import client.services.StyleUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.*;

public class DebtOverviewCtrl implements Initializable{
    private final ServerUtils server;
    private final DebtUtils debt;
    private final StyleUtils style;
    private final MainCtrl mainCtrl;
    private final ErrorService errorService;
    private Event event;
    private Participant picked;
    private Map<Participant, Float> participantDebts;
    private List<Participant> participants;
    private HashMap<String, Object> languageMap;

//    TODO change debt calculation to use buffered transaction/participant lists to minimize server interactions
//    private List<Expense> transactions;
//    private List<Participant> participants;

    @FXML
    private Text eventTotal;
    @FXML
    private Text individualTotal;
    @FXML
    private ComboBox<Participant> participantPicker;
    @FXML
    private Accordion accordion;
    @FXML
    private Button addPaymentButton;
    @FXML
    private Button backButton;
    @FXML
    private Text debtOverviewTitle;
    @FXML
    private Text totalDebt;
    @FXML
    private Text totalEvent;

    /**
     * initialize
     * @param location
     * The location used to resolve relative paths for the root object, or
     * {@code null} if the location is not known.
     *
     * @param resources
     * The resources used to localize the root object, or {@code null} if
     * the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        accordion.getPanes().clear();

        participants = new ArrayList<>();

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
        participantPicker.setConverter(pConverter);
    }

    /**
     * Injectable constructor
     * @param server serverUtils
     * @param debt debtUtils
     * @param style styleUtils
     * @param mainCtrl mainCtrl
     * @param errorService errorService
     */
    @Inject
    public DebtOverviewCtrl(ServerUtils server, DebtUtils debt, StyleUtils style, MainCtrl mainCtrl,
                             ErrorService errorService) {
        this.server = server;
        this.debt = debt;
        this.style = style;
        this.mainCtrl = mainCtrl;
        this.errorService = errorService;
    }

    /**
     * Set the language of the page
     * @param map map which contains translation
     */
    public void setLanguage(HashMap<String, Object> map){
        languageMap = map;
        addPaymentButton.setText((String) map.get("addPaymentButton"));
        backButton.setText((String) map.get("back"));
        debtOverviewTitle.setText((String) map.get("debtTitle"));
        totalDebt.setText((String) map.get("totalDebt"));
        totalEvent.setText((String) map.get("totalEvent"));
        participantPicker.setPromptText((String) map.get("choosePerson"));

        debt.setLanguageMap(map);
        errorService.changeLanguage(map);
        
        mainCtrl.setDynamicButtonSize(addPaymentButton);
        mainCtrl.setDynamicButtonSize(backButton);
    }

    /**
     * refresh this page
     * @param event event
     */
    public void refresh(Event event) {
        this.event = event;
        eventTotal.setText(debt.formattedAmount(debt.expenseTotal(event)));
//        this.transactions = server.getTransactions(event.getInviteCode());
        participantPicker.getSelectionModel().clearSelection();
        repopulate();
    }
    
    /**
     * Refreshes the payment instructions for the currently picked participant
     */
    public void refreshDebtDetails() {
        accordion.getPanes().clear();
        for (Participant participant : participants) {
            if (participantDebts.get(participant) == null) continue;
            float debtAmount = participantDebts.get(participant);
            if (debtAmount == 0) continue;
            accordion.getPanes().add(new TitledPane(
                    debt.getInstructionLine(picked, participant, debtAmount),
                    debt.getTextAreaInfo(participant, debtAmount))
            );
        }
        if (accordion.getPanes().isEmpty()) {
            TitledPane untitled = new TitledPane(languageMap.get("noOpenDebts").toString(), null);
            untitled.setCollapsible(false);
            accordion.getPanes().add(untitled);
        }
    }

    /**
     * onAction for the participant picker to automatically update table and individual total
     */
    public void pick() {
        picked = participantPicker.getValue();
        individualTotal.setText(debt.formattedAmount(debt.groupDebt(event, picked, null)));
        if (picked == null) return;
        this.participantDebts = debt.specificSimplestDebt(event, picked);
        repopulate();
        refreshDebtDetails();
    }

    /**
     * clears and repopulates the participant picker
     */
    public void repopulate() {
        List<Participant> temp = server.getParticipantsByEventInviteCode(event.getInviteCode());
        Platform.runLater(() -> {
            if (picked == null) {
                participantPicker.getItems().clear();
                participantPicker.getItems().addAll(temp);
            }
            participants = temp;
        });
    }
    
    public void settleDebt() {
        mainCtrl.showSettleDebt(event);
    }

    /**
     * Triggered once "back" button is clicked
     * Returns to the event overview
     */
    public void back() {
        try {
            accordion.getPanes().clear();
            mainCtrl.showEventOverview(event);
        } catch (Exception e) {
            Alert alert = errorService.generalError(e.getMessage());
            alert.showAndWait();
        }
    }
}
