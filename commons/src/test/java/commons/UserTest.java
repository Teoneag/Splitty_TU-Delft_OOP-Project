package commons;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void checkConstructor() {
        User testUser = new User(1, "username", "email_field");
        assertNotNull(testUser);
        assertEquals(1, testUser.getId());
        assertEquals("username", testUser.getUsername());
        assertEquals("email_field", testUser.getEmail());
        assertEquals("english", testUser.getDisplayLanguage());
        assertEquals(new ArrayList<Event>(), testUser.getParticipatingEvents());
    }

    @Test
    public void testEmptyConstructor() {
        User testUser = new User();
        assertNotNull(testUser);
    }

    @Test
    public void testGetId() {
        User testUser = new User(1, "username", "email_field");
        assertEquals(1, testUser.getId());
    }

    @Test
    public void testGetUsername() {
        User testUser = new User(1, "username", "email_field");
        assertEquals("username", testUser.getUsername());
    }

    @Test
    public void testGetEmail() {
        User testUser = new User(1, "username", "email_field");
        assertEquals("email_field", testUser.getEmail());
    }

    @Test
    public void testGetParticipant() {
        User testUser = new User(1, "username", "email_field");
        assertEquals("english", testUser.getDisplayLanguage());
    }

    @Test
    public void testGetParticipatingEvents() {
        User testUser = new User(1, "username", "email_field");
        assertEquals(new ArrayList<Event>(), testUser.getParticipatingEvents());
    }
    
    @Test
    public void testSetId() {
        User testUser = new User(1, "username", "email_field");
        testUser.setId(4);
        assertEquals(4, testUser.getId());
    }
    
    @Test
    public void testSetUsername() {
        User testUser = new User(1, "username", "email_field");
        testUser.setUsername("another");
        assertEquals("another", testUser.getUsername());
    }
    
    @Test
    public void testSetEmail() {
        User testUser = new User(1, "username", "email_field");
        testUser.setEmail("new mail");
        assertEquals("new mail", testUser.getEmail());
    }

    @Test
    public void testSetDisplayLanguage() {
        User testUser = new User(1, "username", "email_field");
        testUser.setDisplayLanguage("dutch");
        assertEquals("dutch", testUser.getDisplayLanguage());
    }

    @Test
    public void testSetParticipatingEvents() {
        Event event1 = new Event();
        Event event2 = new Event();
        List<Event> events = List.of(event1, event2);
        User testUser = new User(1, "username", "email_field");
        testUser.setParticipatingEvents(events);
        assertEquals(events, testUser.getParticipatingEvents());
    }

    @Test
    public void testAddEvents() {
        User testUser = new User(1, "username", "email_field");
        Event event1 = new Event();
        testUser.addEvent(event1);
        List<Event> expectedEvents = List.of(event1);
        assertEquals(expectedEvents, testUser.getParticipatingEvents());   

    }

    @Test
    public void testRemoveNonExistentEvent() {
        User testUser = new User(1, "username", "email_field");
        Event event1 = new Event();
        assertFalse(testUser.removeEvent(event1));
        assertEquals(new ArrayList<Event>(), testUser.getParticipatingEvents());
    }

    @Test
    public void testRemoveEvent() {
        User testUser = new User(1, "username", "email_field");
        Event event1 = new Event();
        testUser.addEvent(event1);
        assertTrue(testUser.removeEvent(event1));
        assertEquals(new ArrayList<Event>(), testUser.getParticipatingEvents());
    }

    @Test
    public void testEquals() {
        User testUser = new User(1, "username", "email_field");
        User testUser1 = new User(1, "username", "email_field");
        assertEquals(testUser, testUser1);
    }

    @Test
    public void testEqualsChangedAllParameters() {
        User testUser = new User(1, "username", "email_field");
        Event event1 = new Event();
        testUser.addEvent(event1);
        testUser.setDisplayLanguage("dutch");
        User testUser1 = new User(1, "username", "email_field");
        testUser1.addEvent(event1);
        testUser1.setDisplayLanguage("dutch");
        assertEquals(testUser, testUser1);
    }

    @Test
    public void testEqualsNull() {
        User testUser = new User(1, "username", "email_field");
        assertNotEquals(null, testUser);
    }

    @Test
    public void testNotEqualLanguage() {
        User testUser = new User(1, "username", "email_field");
        User testUser1 = new User(1, "username", "email_field");
        testUser.setDisplayLanguage("dutch");
        assertNotEquals(testUser, testUser1);
    }

    @Test
    public void testNotEqualEvents() {
        User testUser = new User(1, "username", "email_field");
        Event event1 = new Event();
        testUser.addEvent(event1);
        User testUser1 = new User(1, "username", "email_field");
        assertNotEquals(testUser, testUser1);
    }

    @Test
    public void testNotEqualsAll() {
        User testUser = new User(1, "username", "email_field");
        User testUser1 = new User(1, "username", "email_field");
        Event event1 = new Event();
        testUser.setDisplayLanguage("dutch");
        testUser.addEvent(event1);
        assertNotEquals(testUser, testUser1);
    }

    @Test
    void testHashCode() {
        User testUser = new User(1, "username", "email_field");
        User testUser1 = new User(1, "username", "email_field");
        assertEquals(testUser.hashCode(), testUser1.hashCode());
    }

    @Test
    void testNotHashCode() {
        User testUser = new User(1, "username", "email_field");
        User testUser1 = new User(2, "username", "email_field");
        assertNotEquals(testUser.hashCode(), testUser1.hashCode());
    }
}
