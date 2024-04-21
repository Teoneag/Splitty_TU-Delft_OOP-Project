package client.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import commons.Event;
import commons.Expense;
import commons.Participant;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(ApplicationExtension.class)
public class DebtUtilsTest {
    
    private DebtUtils debtUtils;
    
    private Event event;
    
    @Mock
    private ServerUtils server;
    @Mock
    private ConfigService configService;
    
    @BeforeAll
    public static void setUpClass() {
        
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
    }
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(configService.getConfigCurrency()).thenReturn("USD");
        
        event = new Event("EVENT1", "title", "description");
        this.debtUtils = new DebtUtils(server, configService);
    }
    
    @AfterAll
    public static void cleanUp() {
        
    }
    
    @Test
    public void testExpenseTotal() {
        int amount = 10;
        int amount2 = 20;
        int amount3 = 30;
        float expected = amount + amount2 + amount3;
        
        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense(amount, event, "expense1", LocalDate.now(), null, null, null));
        expenses.add(new Expense(amount2, event, "expense2", LocalDate.now(), null, null, null));
        expenses.add(new Expense(amount3, event, "expense3", LocalDate.now(), null, null, null));
        
        when(server.getExpensesByCurrency("EVENT1")).thenReturn(expenses);
        
        assertEquals(expected, debtUtils.expenseTotal(event));
    }
    
    @Test
    public void testFormattedAmount() {
        when(configService.getConfigCurrency()).thenReturn("USD");
        String currencySymbol = Currency.getInstance("USD").getSymbol();
        assertEquals(currencySymbol + " 20.00", debtUtils.formattedAmount(20));
    }
    
    @Test
    public void testGetInstructionLinePositiveAmount() {
        HashMap<String, Object> languageMap = new HashMap<>();
        languageMap.put("owes", " owes ");
        languageMap.put("isOwedBy", " owes ");
        debtUtils.setLanguageMap(languageMap);
        
        Participant picked = new Participant("pickedName", "");
        Participant participant = new Participant("participantName", "");
        
        String currencySymbol = Currency.getInstance("USD").getSymbol();
        String expected = "pickedName owes participantName "+ currencySymbol+ " 20.00";
        
        assertEquals(expected, debtUtils.getInstructionLine(picked, participant, 20));
    }
    
    @Test
    public void testGetInstructionLineNegativeAmount() {
        HashMap<String, Object> languageMap = new HashMap<>();
        languageMap.put("owes", " owes ");
        languageMap.put("isOwedBy", " is owed by ");
        debtUtils.setLanguageMap(languageMap);
        
        Participant picked = new Participant("pickedName", "");
        Participant participant = new Participant("participantName", "");
        
        String currencySymbol = Currency.getInstance("USD").getSymbol();
        String expected = "pickedName is owed by participantName " + currencySymbol + " 20.00";
        
        assertEquals(expected, debtUtils.getInstructionLine(picked, participant, -20));
    }
    
    @Test
    public void testGetTextAreaInfoNoBankDetailsPositiveAmount() {
        
        HashMap<String, Object> languageMap = new HashMap<>();
        languageMap.put("noBankDetails", "No bank details: ");
        
        debtUtils.setLanguageMap(languageMap);
        
        Participant participant = new Participant("participantName", "", "email", "", "", "EVENT1");
        
        TextArea expected = new TextArea();
        expected.setDisable(true);
        expected.setStyle("-fx-text-fill: red");
        expected.setText(languageMap.get("noBankDetails") + "participantName");
        
        TextArea actual = debtUtils.getTextAreaInfo(participant, 20);
        
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getStyle(), actual.getStyle());}
    
    @Test
    public void testGetTextAreaInfoNoEmailNegativeAmount() {
        
        
        HashMap<String, Object> languageMap = new HashMap<>();
        languageMap.put("noMail", " does not have a mail");
        
        debtUtils.setLanguageMap(languageMap);
        
        Participant participant = new Participant("participantName", "", "", "", "", "EVENT1");
        
        TextArea expected = new TextArea();
        expected.setDisable(true);
        expected.setStyle("-fx-text-fill: red");
        expected.setText("participantName" + languageMap.get("noMail"));
        
        TextArea actual = debtUtils.getTextAreaInfo(participant, -1);
        
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getStyle(), actual.getStyle());
    }
    
    @Test
    public void testGetTextAreaInfoEmailNegativeAmount() {
        
        HashMap<String, Object> languageMap = new HashMap<>();
        languageMap.put("contactInformation", "Contact information for: ");
        
        debtUtils.setLanguageMap(languageMap);
        
        Participant participant = new Participant("participantName", "", "mail@mail.com", "", "", "EVENT1");
        
        TextArea expected = new TextArea();
        expected.setDisable(true);
        expected.setText(languageMap.get("contactInformation") + "participantName"
                + "\nEmail: mail@mail.com"
                );
        
        TextArea actual = debtUtils.getTextAreaInfo(participant, -1);
        
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getStyle(), actual.getStyle());
    }
    
    @Test
    public void testGetTextAreaInfoBankPositiveAmount() {
        
        HashMap<String, Object> languageMap = new HashMap<>();
        languageMap.put("bankDetails", "Bank details for: ");
        
        debtUtils.setLanguageMap(languageMap);
        
        Participant participant = new Participant("participantName", "", "mail@mail.com", "iban", "bic", "EVENT1");
        
        TextArea expected = new TextArea();
        expected.setDisable(true);
        expected.setText(languageMap.get("bankDetails") + "participantName"
                + "\nIBAN: iban"
                + "\nBIC: bic"
                + "\nEmail: mail@mail.com"
        );
        
        TextArea actual = debtUtils.getTextAreaInfo(participant, 1);
        
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getStyle(), actual.getStyle());
    }
    
    @Test
    public void testGetTextAreaInfoBankPositiveAmountNoMailNoIban() {
        
        HashMap<String, Object> languageMap = new HashMap<>();
        languageMap.put("bankDetails", "Bank details for: ");
        
        debtUtils.setLanguageMap(languageMap);
        
        Participant participant = new Participant("participantName", "", "", "", "bic", "EVENT1");
        
        TextArea expected = new TextArea();
        expected.setDisable(true);
        expected.setText(languageMap.get("bankDetails") + "participantName"
                + "\nBIC: bic"
        );
        
        TextArea actual = debtUtils.getTextAreaInfo(participant, 1);
        
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getStyle(), actual.getStyle());
    }
    
    
    @Test
    public void testGetTextAreaInfoBankPositiveAmountNoBic() {

        
        HashMap<String, Object> languageMap = new HashMap<>();
        languageMap.put("bankDetails", "Bank details for: ");
        
        debtUtils.setLanguageMap(languageMap);
        
        Participant participant = new Participant("participantName", "", "mail@mail.com", "iban", "", "EVENT1");
        
        TextArea expected = new TextArea();
        expected.setDisable(true);
        expected.setText(languageMap.get("bankDetails") + "participantName"
                + "\nIBAN: iban"
                + "\nEmail: mail@mail.com"
        );
        
        TextArea actual = debtUtils.getTextAreaInfo(participant, 1);
        
        assertEquals(expected.getText(), actual.getText());
        assertEquals(expected.getStyle(), actual.getStyle());
    }
    
    @Test
    public void testGroupDebt() {
        List<Expense> expenses = new ArrayList<>();
        
        Participant sponsor = new Participant("sponsorName", "");
        
        Set<Participant> debtors = new HashSet<>();
        Participant debtor = new Participant("debtorName", "");
        debtors.add(debtor);
        
        LocalDate date = LocalDate.now();
        
        Expense expense1 = new Expense(10, event, "expense1", date, sponsor, debtors, null);
        Expense expense2 = new Expense(15, event, "expense2", date, sponsor, debtors, null);
        expense1.setCurrency(Currency.getInstance("USD"));
        expense2.setCurrency(Currency.getInstance("USD"));
        
        expenses.add(expense1);
        expenses.add(expense2);
        
        float expectedDebtSponsor = -25;
        float expectedDebtDebtor = 25;
        
        when(configService.getConfigCurrency()).thenReturn("USD");
        when(server.getRate("USD", "USD", date.toString())).thenReturn(1f);
        
        assertEquals(expectedDebtSponsor, debtUtils.groupDebt(event, sponsor, expenses));
        assertEquals(expectedDebtDebtor, debtUtils.groupDebt(event, debtor, expenses));
    }
    
    @Test
    public void testGroupDebtNullParticipantNullTransaction() {
        List<Expense> expenses = new ArrayList<>();
        Expense expense1 = new Expense(10, event, "expense1", null, null, null, null);
        expenses.add(expense1);
        
        when(configService.getConfigCurrency()).thenReturn("USD");
        when(server.getTransactions(event.getInviteCode())).thenReturn(expenses);
        
        assertEquals(0, debtUtils.groupDebt(event, null, null));
        verify((server), times(1)).getTransactions(event.getInviteCode());
    }
    
    @Test
    public void testGetAllGroupDebt() {
        List<Expense> expenses = new ArrayList<>();
        
        Participant sponsor = new Participant("sponsorName", "");
        
        Set<Participant> debtors = new HashSet<>();
        Participant debtor = new Participant("debtorName", "");
        debtors.add(debtor);
        
        List<Participant> allParticipants = new ArrayList<>();
        allParticipants.add(sponsor);
        allParticipants.add(debtor);
        
        LocalDate date = LocalDate.now();
        
        Expense expense1 = new Expense(10, event, "expense1", date, sponsor, debtors, null);
        Expense expense2 = new Expense(15, event, "expense2", date, sponsor, debtors, null);
        expense1.setCurrency(Currency.getInstance("USD"));
        expense2.setCurrency(Currency.getInstance("USD"));
        
        expenses.add(expense1);
        expenses.add(expense2);
        
        when(configService.getConfigCurrency()).thenReturn("USD");
        when(server.getRate("USD", "USD", date.toString())).thenReturn(1f);
        when(server.getTransactions(event.getInviteCode())).thenReturn(expenses);
        when(server.getParticipantsByEventInviteCode(event.getInviteCode())).thenReturn(allParticipants);
        
        float expectedDebtSponsor = -25;
        float expectedDebtDebtor = 25;
        
        Map<Participant, Float> expected = new HashMap<>();
        expected.put(sponsor, expectedDebtSponsor);
        expected.put(debtor, expectedDebtDebtor);
        
        assertEquals(expected, debtUtils.getAllGroupDept(event));
    }
    
    @Test
    public void testSpecificSimplestDebt() {
        List<Expense> expenses = new ArrayList<>();
        
        Participant sponsor = new Participant("sponsorName", "");
        
        Set<Participant> debtors = new HashSet<>();
        Participant debtor = new Participant("debtorName", "");
        debtors.add(debtor);
        
        List<Participant> allParticipants = new ArrayList<>();
        allParticipants.add(sponsor);
        allParticipants.add(debtor);
        
        LocalDate date = LocalDate.now();
        
        Expense expense1 = new Expense(10, event, "expense1", date, sponsor, debtors, null);
        Expense expense2 = new Expense(15, event, "expense2", date, sponsor, debtors, null);
        expense1.setCurrency(Currency.getInstance("USD"));
        expense2.setCurrency(Currency.getInstance("USD"));
        
        expenses.add(expense1);
        expenses.add(expense2);
        
        when(configService.getConfigCurrency()).thenReturn("USD");
        when(server.getRate("USD", "USD", date.toString())).thenReturn(1f);
        when(server.getTransactions(event.getInviteCode())).thenReturn(expenses);
        when(server.getParticipantsByEventInviteCode(event.getInviteCode())).thenReturn(allParticipants);
        
        float expectedDebtSponsor = -25;
        float expectedDebtDebtor = 25;
        
        Map<Participant, Float> expectedSimplesDebtSponsor = new HashMap<>();
        expectedSimplesDebtSponsor.put(debtor, expectedDebtSponsor);
        
        Map<Participant, Float> expectedSimplestDebtDebtor = new HashMap<>();
        expectedSimplestDebtDebtor.put(sponsor, expectedDebtDebtor);
        
        assertEquals(expectedSimplesDebtSponsor, debtUtils.specificSimplestDebt(event, sponsor));
        assertEquals(expectedSimplestDebtDebtor, debtUtils.specificSimplestDebt(event, debtor));
    }
    
    
 
}
