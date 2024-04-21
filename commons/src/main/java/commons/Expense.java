package commons;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Set;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity(name = "expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @JoinColumn(foreignKey = @ForeignKey(name = "inviteCode"), nullable = false)
    @ManyToOne
    private Event parentEvent;
    private float amount;
    private Currency currency;
    private String title;
    private LocalDate date;
    @JoinColumn(foreignKey = @ForeignKey(name = "id"))
    @ManyToOne
    private Participant sponsor;
    @ManyToMany
    private Set<Participant> debtors;
    @ManyToOne
    private Tag tag;

    public Expense() {
    }

    /**
     * Reduced param constructor for use in scenes
     *
     * @param amount      amount paid by the sponsor (euro by default)
     * @param parentEvent parent event within which the expense exists
     * @param title       title of expense
     * @param date        date of expense
     * @param sponsor     Participant who paid for the expense
     * @param debtors     Participants who owe the sponsor
     * @param tag         tag
     */
    public Expense(float amount, Event parentEvent, String title, LocalDate date,
                   Participant sponsor, Set<Participant> debtors, Tag tag) {
        this.amount = amount;
        this.parentEvent = parentEvent;
        this.title = title;
        this.date = date;
        this.sponsor = sponsor;
        this.debtors = debtors;
        this.tag = tag;
    }

    /**
     * Full param constructor just in case
     *
     * @param id          integer id for database
     * @param parentEvent parent event within which the expense exists
     * @param amount      amount paid by the sponsor (euro)
     * @param title       title of expense
     * @param date        date of expense
     * @param sponsor     Participant who paid for the expense
     * @param debtors     Participants who have to pay the sponsor back
     * @param tag         tag
     */
    public Expense(int id, Event parentEvent, float amount, String title, LocalDate date, Participant sponsor,
                   Set<Participant> debtors, Tag tag) {
        this.id = id;
        this.parentEvent = parentEvent;
        this.amount = amount;
        this.title = title;
        this.date = date;
        this.sponsor = sponsor;
        this.debtors = debtors;
        this.tag = tag;
    }

    /**
     * Split the debt evenly among all debtors
     *
     * @return amount divided by the number of debtors.
     * If no debtors exist, return the full amount
     */
    public float amountPerDebtor() {
        if (debtors.isEmpty()) return amount;
        return amount / (float) debtors.size();
    }

    /**
     * Get the formatted amount string for this expense
     *
     * @return a formatted amount string with two decimal places and a currency prefix
     */
    public String formattedAmount() {
        DecimalFormat df = new DecimalFormat("#0.00");
        return currency.getSymbol() + " " + df.format(amount);
    }

    /**
     * Adds a Participant to the debtor set
     *
     * @param debtor Participant to add
     * @return true on successful add, false if already in set or Set.add() otherwise fails
     */
    public boolean addDebtor(Participant debtor) {
        if (debtor == null) return false;
        return debtors.add(debtor);
    }

    /**
     * Removes a Participant from the debtor set
     *
     * @param debtor Participant to remove
     * @return true on successful remove, false if not in set or Set.remove() otherwise fails
     */
    public boolean removeDebtor(Participant debtor) {
        if (debtor == null) return false;
        return debtors.remove(debtor);
    }

    public long getId() {
        return id;
    }

    public Event getParentEvent() {
        return parentEvent;
    }

    public void setParentEvent(Event event) {
        this.parentEvent = event;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Participant getSponsor() {
        return sponsor;
    }

    public void setSponsor(Participant sponsor) {
        this.sponsor = sponsor;
    }

    public Set<Participant> getDebtors() {
        return debtors;
    }

    public void setDebtors(Set<Participant> debtors) {
        this.debtors = debtors;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }

}
