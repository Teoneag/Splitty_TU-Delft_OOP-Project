package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventRequestTest {
    private EventRequest eventRequest;

    @BeforeEach
    void setUp() {
        // Initialize EventRequest with some default values
        eventRequest = new EventRequest("Sample Title", "Sample Description");
    }

    @Test
    void testConstructor() {
        // Testing parameterized constructor
        assertEquals("Sample Title", eventRequest.getTitle(), "Constructor should properly set title.");
        assertEquals("Sample Description", eventRequest.getDescription(), "Constructor should properly set description.");
    }

    @Test
    void testDefaultConstructor() {
        // Testing default constructor by creating a new instance of EventRequest
        EventRequest defaultEventRequest = new EventRequest();
        defaultEventRequest.setTitle("Default Title");
        defaultEventRequest.setDescription("Default Description");

        assertEquals("Default Title", defaultEventRequest.getTitle(), "Default constructor should allow setting title afterwards.");
        assertEquals("Default Description", defaultEventRequest.getDescription(), "Default constructor should allow setting description afterwards.");
    }

    @Test
    void testSetTitle() {
        // Set a new title and verify it's updated
        String newTitle = "New Event Title";
        eventRequest.setTitle(newTitle);
        assertEquals(newTitle, eventRequest.getTitle(), "setTitle should update the title.");
    }

    @Test
    void testSetDescription() {
        // Set a new description and verify it's updated
        String newDescription = "New Event Description";
        eventRequest.setDescription(newDescription);
        assertEquals(newDescription, eventRequest.getDescription(), "setDescription should update the description.");
    }
}
