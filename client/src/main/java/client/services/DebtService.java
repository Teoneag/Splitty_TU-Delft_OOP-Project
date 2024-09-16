package client.services;

import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import javafx.scene.control.TextArea;

import java.text.DecimalFormat;
import java.util.*;

final public class DebtService {

    private final ServerUtils server;
    private final ConfigService configService;
    private final I18NService i8NService;

    /**
     * constructor
     *
     * @param server        serverUtils
     * @param configService configService
     */
    @Inject
    public DebtService(ServerUtils server, ConfigService configService, I18NService i18NService) {
        this.server = server;
        this.configService = configService;
        this.i8NService = i18NService;
    }

    /**
     * Gives the sum total of all expenses across an event (ignores currency)
     *
     * @param event the event
     * @return the sum of all expense amounts
     */
    public float expenseTotal(Event event) {
        if (event == null || event.getInviteCode() == null) return 0;
        float total = 0;

        total += (float) server.getExpensesByCurrency(event.getInviteCode())
            .stream()
            .mapToDouble(Expense::getAmount)
            .sum();

        return total;
    }
    
    public String getUsedCurrency() {
        return configService.getConfigCurrency();
    }

    /**
     * Gives the net debt of the participant to the whole event group (ignores currency)
     *
     * @param event        event
     * @param participant  person who (supposedly) owes the group money
     * @param transactions the transactions to be used for the calculation
     * @return positive if the participant owes the group, negative if they are owed
     */
    public float groupDebt(Event event, Participant participant, List<Expense> transactions) {
        if (transactions == null) transactions = server.getTransactions(event.getInviteCode());
        String userCurrency = configService.getConfigCurrency();
        float debt = 0;

        if (participant != null) {
            // Expenses debtor needs to repay & payments they have received
            debt += (float) transactions
                .stream()
                .filter(e -> e.getDebtors().contains(participant))
                .mapToDouble(e -> server.getRate(e.getCurrency().toString(), userCurrency, e.getDate().toString())
                    * e.amountPerDebtor())
                .sum();

            // Expenses debtor paid for & payments they have sent
            debt -= (float) transactions
                .stream()
                .filter(e -> e.getSponsor().equals(participant))
                .mapToDouble(e -> server.getRate(e.getCurrency().toString(), userCurrency, e.getDate().toString())
                    * e.getAmount())
                .sum();
        }

        return debt;
    }


    /**
     * Gets a map of all the participants and the total debt they have towards the event
     *
     * @param event the event to get the map for
     * @return the map of participant to debt for the event
     */
    public Map<Participant, Float> getAllGroupDept(Event event) {
        List<Expense> transactions = server.getTransactions(event.getInviteCode());
        List<Participant> participants = server.getParticipantsByEventInviteCode(event.getInviteCode());
        Map<Participant, Float> totalDeptPerPerson = new HashMap<>();
        for (Participant participant : participants) {
            totalDeptPerPerson.put(participant, groupDebt(event, participant, transactions));
        }
        return totalDeptPerPerson;
    }


    /**
     * Gets the specific debts for a participants in such a way that it has the least amount of transactions
     *
     * @param event             the event to calculate the debt for
     * @param targetParticipant the participant to get the debts for
     * @return the people and debt the participant owes
     */
    public Map<Participant, Float> specificSimplestDebt(Event event, Participant targetParticipant) {
        Map<Participant, Float> simplestExpenses = new HashMap<>();

        Map<Participant, Float> groupDebts = getAllGroupDept(event);
        List<Participant> debtors = new ArrayList<>();
        List<Participant> sponsors = new ArrayList<>();

        for (Participant participant : groupDebts.keySet()) {
            if (groupDebts.get(participant) > 0) debtors.add(participant);
            if (groupDebts.get(participant) < 0) sponsors.add(participant);
        }

        sponsors.sort((x, y) -> Float.compare(groupDebts.get(x), groupDebts.get(y)));
        debtors.sort((x, y) -> Float.compare(groupDebts.get(y), groupDebts.get(x)));

        int i = 0;
        while (!sponsors.isEmpty() && !debtors.isEmpty()) {
            Participant debtor = debtors.getFirst();
            Participant sponsor = sponsors.getFirst();

            float transferAmount = Math.min(groupDebts.get(debtor), -1 * groupDebts.get(sponsor));
            if (debtor.equals(targetParticipant)) {
                simplestExpenses.put(sponsor, transferAmount);
            }
            if (sponsor.equals(targetParticipant)) {
                simplestExpenses.put(debtor, -1 * transferAmount);
            }

            groupDebts.put(sponsor, groupDebts.get(sponsor) + transferAmount);
            groupDebts.put(debtor, groupDebts.get(debtor) - transferAmount);

            if (groupDebts.get(debtor) == 0) {
                debtors.remove(debtor);
            }
            if (groupDebts.get(sponsor) == 0) {
                sponsors.remove(sponsor);
            }
        }

        return simplestExpenses;
    }

    /**
     * format a float into a currency string
     *
     * @param amount amount
     * @return formatted currency string
     */
    public String formattedAmount(float amount) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return Currency.getInstance(configService.getConfigCurrency()).getSymbol() + " " +
            df.format(amount);
    }

    /**
     * Getter to format the instruction given two participants and the amount
     *
     * @param picked      the main participant
     * @param participant the other person involved in the transaction
     * @param amount      the amount that should be paid
     * @return the instruction as a string
     */
    public String getInstructionLine(Participant picked, Participant participant, float amount) {
        if (amount > 0) {
            return picked.getFullName() + " " + i8NService.get("owes")  + " "
                + participant.getFullName() + " " + formattedAmount(amount);
        } else {
            return picked.getFullName() + " " + i8NService.get("is.owed.by") + " " + participant.getFullName()
                + " " + formattedAmount(-1 * amount);
        }
    }

    /**
     * Returns the expandable info for the debt overview as fxml element, styled with the correct colour
     * If the picked participant owes money then the info will include the bank details of the sponsor if available
     * Is the picked participant is owed money, the contact information of the debtor is showed
     *
     * @param participant participant that owes/is owed by the picked participant
     * @param amount      the amount that is owed
     * @return the string with the required information as described
     */
    public TextArea getTextAreaInfo(Participant participant, float amount) {
        TextArea res = new TextArea();
        res.setDisable(true);
        if (amount > 0) {
            if (participant.getBic().isEmpty() && participant.getIban().isEmpty()) {
                res.setText(i8NService.get("noBankDetails") + " " + participant.getFullName());
                res.setStyle("-fx-text-fill: red");
                return res;
            }
            res.setText(i8NService.get("bankDetails") + " " + participant.getFullName()
                + ((participant.getIban().isEmpty()) ? "" : "\nIBAN: " + participant.getIban())
                + (participant.getBic().isEmpty() ? "" : "\nBIC: " + participant.getBic())
                + (participant.getEmail().isEmpty() ? "" : "\nEmail: " + participant.getEmail()));
        } else {
            if (participant.getEmail().isEmpty()) {
                res.setText(participant.getFullName() + " " + i8NService.get("noMail"));
                res.setStyle("-fx-text-fill: red");
                return res;
            }
            res.setText(i8NService.get("contactInformation") + " " + participant.getFullName()
                + "\nEmail: " + participant.getEmail());
        }
        res.setStyle("-fx-text-fill: white");
        return res;
    }
}
