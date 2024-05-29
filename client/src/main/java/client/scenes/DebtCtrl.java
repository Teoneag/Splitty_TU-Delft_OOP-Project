package client.scenes;

import client.services.*;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DebtCtrl implements Initializable {
    private final ServerUtils server;
    private final DebtService debt;
    private final MainCtrl mainCtrl;
    private final ErrorService errorService;
    private final I18NService i18NService;

    private Event event;
    private Participant picked;
    private Map<Participant, Float> participantDebts;
    private List<Participant> participants;

//    TODO change debt calculation to use buffered transaction/participant lists to minimize server interactions

    @FXML
    private Label debtOverviewLabel;
    @FXML
    private Label eventTotalLabel;
    @FXML
    private ComboBox<Participant> chosePerson;
    @FXML
    private Label totalDebtLabel;
    @FXML
    private Accordion accordion;
    @FXML
    private Button SettleDebtsButton;

    /**
     * Injectable constructor
     *
     * @param server       serverUtils
     * @param debt         debtUtils
     * @param style        styleUtils
     * @param mainCtrl     mainCtrl
     * @param errorService errorService
     */
    @Inject
    public DebtCtrl(ServerUtils server, DebtService debt, StyleService style, MainCtrl mainCtrl,
                    ErrorService errorService, I18NService i18NService) {
        this.server = server;
        this.debt = debt;
        this.mainCtrl = mainCtrl;
        this.errorService = errorService;
        this.i18NService = i18NService;
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
        chosePerson.setConverter(pConverter);
    }

    /**
     * Set the language of the page
     */
    public void setLanguage() {
        i18NService.setTranslation(debtOverviewLabel, "debt.overview");
        i18NService.setTranslation(chosePerson, "chose.person");
        i18NService.setTranslation(SettleDebtsButton, "settle.debts");
    }

    /**
     * refresh this page
     *
     * @param event event
     */
    public void refresh(Event event) {
        this.event = event;
        i18NService.setTranslation(eventTotalLabel, "event.total", debt.formattedAmount(debt.expenseTotal(event)));
//        this.transactions = server.getTransactions(event.getInviteCode());
        chosePerson.getSelectionModel().clearSelection();
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
            // ToDo
            TitledPane untitled = new TitledPane(i18NService.get("noOpenDebts"), null);
            untitled.setCollapsible(false);
            accordion.getPanes().add(untitled);
        }
    }

    /**
     * onAction for the participant picker to automatically update table and individual total
     */
    public void pick() {
        picked = chosePerson.getValue();
        i18NService.setTranslation(totalDebtLabel, "total.debt",
            debt.formattedAmount(debt.groupDebt(event, picked, null)));
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
                chosePerson.getItems().clear();
                chosePerson.getItems().addAll(temp);
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
            mainCtrl.showEvent(event);
        } catch (Exception e) {
            Alert alert = errorService.generalError(e.getMessage());
            alert.showAndWait();
        }
    }
}
