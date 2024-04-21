package commons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TagTest {
    private Tag tag;
    private Event mockEvent;

    @BeforeEach
    public void setUp() {
        mockEvent = mock(Event.class);
        tag = new Tag("TestTag", 12345, mockEvent);
    }

    /**
     * Test the empty constructor
     */
    @Test
    public void testEmptyConstructor() {
        Tag emptyTag = new Tag();
        assertNotNull(emptyTag, "Check if empty constructor creates object");
    }

    @Test
    public void testGettersAndSetters() {
        // Test id setter and getter
        tag.setId(1L);
        assertEquals(1L, tag.getId(), "Check id");

        // Test name setter and getter
        tag.setName("NewName");
        assertEquals("NewName", tag.getName(), "Check name");

        // Test color setter and getter
        tag.setColor(54321);
        assertEquals(54321, tag.getColor(), "Check color");

        // Test event setter and getter
        Event newEvent = mock(Event.class);
        tag.setEvent(newEvent);
        assertEquals(newEvent, tag.getEvent(), "Check event");
    }

    @Test
    public void testEquals() {
        Tag sameTag = new Tag("TestTag", 12345, mockEvent);
        Tag differentTag = new Tag("Different", 54321, mockEvent);

        assertEquals(tag, sameTag, "Check if tags are equal");
        assertNotEquals(tag, differentTag, "Check if tags are not equal");
    }

    @Test
    public void testHashCode() {
        Tag sameTag = new Tag("TestTag", 12345, mockEvent);
        assertEquals(tag.hashCode(), sameTag.hashCode(), "Check hash code consistency");
    }

    @Test
    public void testToString() {
        assertNotNull(tag.toString(), "Check toString is not null");
    }
}
