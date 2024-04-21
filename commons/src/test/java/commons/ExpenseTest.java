package commons;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.time.LocalDate;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {
    /**
     * Test of the empty constructor in the expense class
     */
    @Test
    void testEmptyConstructor(){
        Expense e = new Expense();
        assertNotNull(e);
    }

    /**
     * Test of the second construcor in the expense class
     */
    @Test
    void otherConstructor(){
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1, event, 1, "testTitle", LocalDate.now(), sponsor, debtors, null);
        assertNotNull(e);
    }

    /**
     * Test of getId method, of class Expense.
     */
    @Test
    void getId() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        assertEquals(0, e.getId());
    }

    /**
     * Test of the get parent event
     */
    @Test
    void getParentEvent(){
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        assertEquals(event, e.getParentEvent());
    }

    /**
     * Test of getAmount method, of class Expense.
     */
    @Test
    void getAmount() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        assertEquals(1, e.getAmount());
    }

    /**
     * Test of setAmount method, of class Expense.
     */
    @Test
    void setAmount() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        e.setAmount(90);
        assertEquals(90, e.getAmount());
    }

    /**
     * Test of getAmountPerDebtor method, of class Expense.
     */
    @Test
    void getAmountPerDebtor() {
        Set<Participant> debtors = new HashSet<>();
        Participant p1 = new Participant("Hugo","Klijn");
        Participant p2 = new Participant("Sofyan","Ali");
        debtors.add(p1);
        debtors.add(p2);
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense((float) 24.54,event,"hello", LocalDate.now(), sponsor, debtors, null);

        float x = (float) 24.54;

        assertEquals((x/2), e.amountPerDebtor());
    }

    /**
     * Test of getFormattedAmount method, of class Expense.
     */
    @Test
    void testFormattedAmount() {
        Set<Participant> debtors = new HashSet<>();
        Participant p1 = new Participant("Hugo", "Klijn");
        Participant p2 = new Participant("Sofyan", "Ali");
        debtors.add(p1);
        debtors.add(p2);
        Participant sponsor = new Participant("a", "b");
        Event event = new Event("BOOB69", "testTitle", "testDesc");
        Expense e = new Expense((float) 24.54, event, "hello", LocalDate.now(), sponsor, debtors, null);

        // Set the currency to Euro
        e.setCurrency(Currency.getInstance("EUR"));

        // Use the Unicode for the Euro symbol to avoid encoding issues
        assertEquals("\u20AC 24.54", e.formattedAmount());
    }

    /**
     * Test of setParentEvent method, of class Expense.
     */
    @Test
    void setParentEvent() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event1 = new Event("BOOB69", "testTitle","testDesc");
        Event event2 = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event1,"hello", LocalDate.now(), sponsor, debtors, null);
        e.setParentEvent(event2);
        assertEquals(event2, e.getParentEvent());
    }

    /**
     * Test of getCurrency method, of class Expense.
     */
    @Test
    void getCurrency() {
        Set<Participant> debtors = new HashSet<>();
        Participant p1 = new Participant("Hugo", "Klijn");
        Participant p2 = new Participant("Sofyan", "Ali");
        debtors.add(p1);
        debtors.add(p2);
        Participant sponsor = new Participant("a", "b");
        Event event = new Event("BOOB69", "testTitle", "testDesc");
        Expense e = new Expense((float) 24.54, event, "hello", LocalDate.now(), sponsor, debtors, null);

        e.setCurrency(Currency.getInstance("EUR"));
        assertEquals(Currency.getInstance("EUR"), e.getCurrency());
    }


    /**
     * Test of getTitle method, of class Expense.
     */
    @Test
    void getTitle() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        assertEquals("hello", e.getTitle());
    }

    /**
     * Test of setTitle method, of class Expense.
     */
    @Test
    void setTitle() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        e.setTitle("TestTitle");
        assertEquals("TestTitle", e.getTitle());
    }

    /**
     * Test of getDate method, of class Expense.
     */
    @Test
    void getDate() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        LocalDate date = LocalDate.now();
        Expense e = new Expense(1, event,"hello", date, sponsor, debtors, null);
        assertEquals(date, e.getDate());
    }

    /**
     * Test of setDate method, of class Expense.
     */
    @Test
    void setDate() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1, event,"hello", LocalDate.MIN, sponsor, debtors, null);
        LocalDate date = LocalDate.now();
        e.setDate(date);
        assertEquals(date, e.getDate());
    }

    /**
     * Test of getSponsor method, of class Expense.
     */
    @Test
    void getSponsor() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        assertEquals(sponsor, e.getSponsor());
    }

    /**
     * Test of setSponsor method, of class Expense.
     */
    @Test
    void setSponsor() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor1 = new Participant("a","b");
        Participant sponsor2 = new Participant("c","d");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor1, debtors, null);

        e.setSponsor(sponsor2);
        assertEquals(sponsor2, e.getSponsor());
    }

    /**
     * Test of getDebtors method, of class Expense.
     */
    @Test
    void getDebtors() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        assertEquals(debtors, e.getDebtors());
    }

    /**
     * Test of setTag method, of class Expense.
     */
    @Test
    void setTag() {
        Set<Participant> debtors1 = new HashSet<>();
        Set<Participant> debtors2 = new HashSet<>();
        Participant sponsor1 = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor1, debtors1, null);
        e.setDebtors(debtors2);
        assertEquals(debtors2, e.getDebtors());
    }

    /**
     * Test of getTag method, of class Expense.
     */
    @Test
    void getTag() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Tag tag = new Tag("test", new Color(1).getRGB(), event);
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, tag);
        assertEquals(tag, e.getTag());
    }

    /**
     * Test of setDebtors method, of class Expense.
     */
    @Test
    void setDebtors() {
        Set<Participant> debtors1 = new HashSet<>();
        Participant sponsor1 = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Tag tag = new Tag("test", new Color(0x25257C).getRGB(), event);
        Tag tag2 = new Tag("Test", new Color(0x7C7C1C).getRGB(), event);
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor1, debtors1, tag);
        e.setTag(tag2);
        assertEquals(tag2, e.getTag());
    }

    /**
     * Test of addDebtor method, of class Expense.
     */
    @Test
    void addDebtorInitial() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);

        Participant debtor = new Participant("Firstname", "Lastname");
        e.addDebtor(debtor);

        assertEquals(1, debtors.size());
    }

    /**
     * Test of removeDebtor method, of class Expense.
     */
    @Test
    void removeDebtor() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);

        Participant debtor = new Participant("Firstname", "Lastname");
        e.addDebtor(debtor);
        e.removeDebtor(debtor);

        assertEquals(0, debtors.size());
    }


    /**
     * Test of equals method, of class Expense.
     */
    @Test
    void testEquals() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e1 = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        Expense e2 = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        assertEquals(e1, e2);
    }

    /**
     * Test of hashCode method, of class Expense.
     */
    @Test
    void testHashCode() {
        Set<Participant> debtors = new HashSet<>();
        Participant sponsor = new Participant("a","b");
        Event event = new Event("BOOB69", "testTitle","testDesc");
        Expense e1 = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        Expense e2 = new Expense(1,event,"hello", LocalDate.now(), sponsor, debtors, null);
        assertEquals(e1.hashCode(), e2.hashCode());
    }


    /**
     * Test of toString method, of class Expense.
     */
    @Test
    void testToString() {
        // Setup the participants, event, and expense
        Participant sponsor = new Participant("a", "b");
        Event event = new Event("BOOB69", "testTitle", "testDesc");
        Set<Participant> debtors = new HashSet<>();
        Expense e = new Expense(1.0f, event, "hello", LocalDate.of(2024, 4, 14), sponsor, debtors, null);

        // Get the output from toString()
        String actualOutput = e.toString();

        // Assertions on parts of the string that do not include dynamic memory addresses
        assertTrue(actualOutput.contains("amount=1.0"));
        assertTrue(actualOutput.contains("currency=<null>"));
        assertTrue(actualOutput.contains("date=2024-04-14"));
        assertTrue(actualOutput.contains("debtors=[]"));
        assertTrue(actualOutput.contains("id=0"));
        assertTrue(actualOutput.contains("title=hello"));
        assertTrue(actualOutput.contains("inviteCode=BOOB69"));
        assertTrue(actualOutput.contains("description=testDesc"));
        assertTrue(actualOutput.contains("firstName=a"));
        assertTrue(actualOutput.contains("lastName=b"));
    }

}