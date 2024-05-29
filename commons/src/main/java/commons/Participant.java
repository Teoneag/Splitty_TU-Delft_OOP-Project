package commons;

import jakarta.persistence.GenerationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity(name = "participant")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String iban;
    private String bic;
    private String eventInviteCode;

    /**
     * Constructor with just the name
     *
     * @param firstName firstName
     * @param lastName  lastName
     */
    public Participant(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Constructor with the parameters needed for the server
     *
     * @param firstName       - first name of the participant
     * @param lastName        - last name of the participant
     * @param email           - the email of the participant
     * @param iban            - The IBAN of the participant
     * @param bic             - the BIC of the participant
     * @param eventInviteCode - the invite code of the event the participant is a part of
     */
    public Participant(String firstName, String lastName, String email,
                       String iban, String bic, String eventInviteCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.iban = iban;
        this.bic = bic;
        this.eventInviteCode = eventInviteCode;
    }

    /**
     * Constructor with all parameters of the class
     *
     * @param id              - the unique id of the participant
     * @param firstName       - first name of the participant
     * @param lastName        - last name of the participant
     * @param email           - the email of the participant
     * @param iban            - The IBAN of the participant
     * @param bic             - the BIC of the participant
     * @param eventInviteCode - the invite code of the event the Participant is a part of
     */
    public Participant(long id, String firstName, String lastName, String email, String iban,
                       String bic, String eventInviteCode) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.iban = iban;
        this.bic = bic;
        this.eventInviteCode = eventInviteCode;
    }

    @SuppressWarnings("unused")
    public Participant() {
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return lastName == null || lastName.isBlank() ? firstName : firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getEventInviteCode() {
        return eventInviteCode;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public void setEventInviteCode(String eventInviteCode) {
        this.eventInviteCode = eventInviteCode;
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