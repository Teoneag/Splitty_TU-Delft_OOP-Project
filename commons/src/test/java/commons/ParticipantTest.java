package commons;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    @Test
    void testFullNameConstruction() {
        Participant participant = new Participant("John", "Doe");
        assertEquals("John Doe", participant.getFullName(), "Full name should be correctly concatenated");
    }

    @Test
    void testFullNameNullLastName() {
        Participant participant = new Participant("John", null);
        assertEquals("John", participant.getFullName(), "Full name should handle null lastName");
    }

    @Test
    void testConstructorWithEmailAndBankDetails() {
        Participant participant = new Participant("Jane", "Doe", "jane.doe@example.com", "DE89 3704 0044 0532 0130 00", "COBADEFFXXX", "INV123");
        assertAll(
                () -> assertEquals("Jane", participant.getFirstName(), "FirstName should match the constructor input"),
                () -> assertEquals("Doe", participant.getLastName(), "LastName should match the constructor input"),
                () -> assertEquals("jane.doe@example.com", participant.getEmail(), "Email should match the constructor input"),
                () -> assertEquals("DE89 3704 0044 0532 0130 00", participant.getIban(), "IBAN should match the constructor input"),
                () -> assertEquals("COBADEFFXXX", participant.getBic(), "BIC should match the constructor input"),
                () -> assertEquals("INV123", participant.getEventInviteCode(), "Event invite code should match the constructor input")
        );
    }

    @Test
    void testSettersAndGetters() {
        Participant participant = new Participant();
        participant.setId(12345);
        participant.setFirstName("John");
        participant.setLastName("Doe");
        participant.setEmail("john.doe@example.com");
        participant.setIban("DE89 3704 0044 0532 0130 00");
        participant.setBic("COBADEFFXXX");
        participant.setEventInviteCode("INV123");

        assertAll(
                () -> assertEquals(12345, participant.getId(), "ID should be set and retrieved correctly"),
                () -> assertEquals("John", participant.getFirstName(), "FirstName should be set and retrieved correctly"),
                () -> assertEquals("Doe", participant.getLastName(), "LastName should be set and retrieved correctly"),
                () -> assertEquals("john.doe@example.com", participant.getEmail(), "Email should be set and retrieved correctly"),
                () -> assertEquals("DE89 3704 0044 0532 0130 00", participant.getIban(), "IBAN should be set and retrieved correctly"),
                () -> assertEquals("COBADEFFXXX", participant.getBic(), "BIC should be set and retrieved correctly"),
                () -> assertEquals("INV123", participant.getEventInviteCode(), "Event invite code should be set and retrieved correctly")
        );
    }

    @Test
    void testToString() {
        Participant sponsor = new Participant("a", "b");
        Event event = new Event("BOOB69", "testTitle", "testDesc");
        Set<Participant> debtors = new HashSet<>();
        Expense e = new Expense(1.0f, event, "hello", LocalDate.of(2024, 4, 14), sponsor, debtors, null);

        String actualOutput = e.toString();

        assertTrue(actualOutput.contains("amount=1.0"), "Output should include the expense amount");
        assertTrue(actualOutput.contains("currency=<null>"), "Output should indicate null currency");
        assertTrue(actualOutput.contains("date=2024-04-14"), "Output should include the expense date");
        assertTrue(actualOutput.contains("debtors=[]"), "Output should list debtors as empty");
        assertTrue(actualOutput.contains("id=0"), "Output should indicate default ID value");
        assertTrue(actualOutput.contains("title=hello"), "Output should include the expense title");
        assertTrue(actualOutput.contains("inviteCode=BOOB69"), "Output should include the event invite code");
        assertTrue(actualOutput.contains("description=testDesc"), "Output should include the event description");
        assertTrue(actualOutput.contains("firstName=a"), "Output should include the sponsor's first name");
        assertTrue(actualOutput.contains("lastName=b"), "Output should include the sponsor's last name");
    }

    @Test
    void testEquality() {
        Participant p1 = new Participant(1, "John", "Doe", "john.doe@example.com", "DE89 3704 0044 0532 0130 00", "COBADEFFXXX", "INV123");
        Participant p2 = new Participant(1, "John", "Doe", "john.doe@example.com", "DE89 3704 0044 0532 0130 00", "COBADEFFXXX", "INV123");
        assertEquals(p1, p2, "Two participants with the same details should be equal");
    }

    @Test
    void testHashCodeConsistency() {
        Participant participant = new Participant("John", "Doe");
        int initialHashCode = participant.hashCode();
        assertEquals(initialHashCode, participant.hashCode(), "HashCode should be consistent");
    }
}
