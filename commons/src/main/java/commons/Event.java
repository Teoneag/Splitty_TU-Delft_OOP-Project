package commons;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.time.LocalDate;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

/**
 * Any event that involved a group of participants, which have shared expenses
 */
@Entity(name = "event")
public class Event implements Serializable {

    @Id
    private String inviteCode;
    private String title;
    private String description;
    private long sId;
    private LocalDate creationDate;
    private LocalDate lastModified;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
        this.lastModified = LocalDate.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastModified = LocalDate.now();
    }

    public Event() {
    }

    /**
     * Constructor for an event
     *
     * @param inviteCode  the invite code of the event
     * @param title       the title of the event
     * @param description the description of the event
     */
    public Event(String inviteCode, String title, String description) {
        this.title = title;
        this.inviteCode = inviteCode;
        this.description = description;
        this.creationDate = LocalDate.now();
        this.lastModified = LocalDate.now();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public long getsId() {
        return sId;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getLastModified() {
        return lastModified;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public void setsId(long sId) {
        this.sId = sId;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastModified(LocalDate lastModified) {
        this.lastModified = lastModified;
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
