package commons;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class User {

    private int id;
    private String username;
    private String email;
    private String displayLanguage;
    private List<Event> participatingEvents;

    /**
     * Constructor for the User class
     *
     * @param id       - the id of the user
     * @param username - the username of the user
     * @param email    - the email of the user
     */
    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        participatingEvents = new ArrayList<>();
        displayLanguage = "english";
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayLanguage() {
        return displayLanguage;
    }

    public List<Event> getParticipatingEvents() {
        return participatingEvents;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplayLanguage(String language) {
        displayLanguage = language;
    }

    public void setParticipatingEvents(List<Event> newEvents) {
        participatingEvents = newEvents;
    }

    public void addEvent(Event event) {
        participatingEvents.add(event);
    }

    public boolean removeEvent(Event event) {
        return participatingEvents.remove(event);
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
