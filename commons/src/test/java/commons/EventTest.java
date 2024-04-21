package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private Event event;
    private final LocalDate testDate = LocalDate.now();

    @BeforeEach
    void setUp() {
        event = new Event("BOOB69", "Test Event", "This is a test event");
    }

    @Test
    void testParameterizedConstructor() {
        assertEquals("BOOB69", event.getInviteCode());
        assertEquals("Test Event", event.getTitle());
        assertEquals("This is a test event", event.getDescription());
        assertEquals(LocalDate.now(), event.getCreationDate());
        assertEquals(LocalDate.now(), event.getLastModified());
    }

    @Test
    void testGettersAndSetters() {
        event.setTitle("New Title");
        assertEquals("New Title", event.getTitle());

        String newDescription = "Updated description for the event.";
        event.setDescription(newDescription);
        assertEquals(newDescription, event.getDescription());

        String newInviteCode = "NEWINVITECODE123";
        event.setInviteCode(newInviteCode);
        assertEquals(newInviteCode, event.getInviteCode());

        long newSId = 12345L;
        event.setsId(newSId);
        assertEquals(newSId, event.getsId());
    }

    @Test
    void testOnCreate() {
        Event newEvent = new Event();
        newEvent.onCreate();
        assertEquals(LocalDate.now(), newEvent.getCreationDate());
        assertEquals(LocalDate.now(), newEvent.getLastModified());
    }

    @Test
    void testOnUpdate() {
        event.onUpdate();
        assertEquals(LocalDate.now(), event.getLastModified());
    }

    @Test
    void testSetCreationDate() {
        LocalDate newCreationDate = LocalDate.of(2022, 1, 1);
        event.setCreationDate(newCreationDate);
        assertEquals(newCreationDate, event.getCreationDate(), "The setCreationDate method should update the creationDate property.");
    }

    @Test
    void testSetLastModified() {
        LocalDate newLastModifiedDate = LocalDate.of(2022, 2, 2);
        event.setLastModified(newLastModifiedDate);
        assertEquals(newLastModifiedDate, event.getLastModified(), "The setLastModified method should update the lastModified property.");
    }


    @Test
    void testEqualsAndHashCode() {
        Event event2 = new Event("BOOB69", "Test Event", "This is a test event");
        assertTrue(event.equals(event2));
        assertEquals(event.hashCode(), event2.hashCode());
    }

    @Test
    void testToString() {
        String resultString = event.toString();
        assertNotNull(resultString);
        assertTrue(resultString.contains("Test Event"));
    }
}
