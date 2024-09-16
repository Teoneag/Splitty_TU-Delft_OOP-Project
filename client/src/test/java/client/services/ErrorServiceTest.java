package client.services;


import com.google.inject.Inject;
import commons.Expense;
import commons.Participant;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
class ErrorServiceTest {

    @InjectMocks
    private ConfigService configService;
    private ErrorService errorService;
    
    private I18NService i18NService;
    @InjectMocks
    private HashMap<String, Object> map;
    @Mock
    private Alert alert;

    @BeforeEach
    public void setup() {
        i18NService = new I18NService();
        i18NService.setLocale(Locale.of("nl", "NL"));
        errorService = new ErrorService(i18NService, new StyleService(i18NService));
        //map = configService.readJsonToMap("src/main/java/client/languages/Messages_nl.json");
        //errorService.changeLanguage(map);

    }

    @BeforeAll
    public static void setUpClass()  {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
    }

    @Test
    public void testPassword() {
        Platform.runLater(() -> {
            alert = errorService.wrongPassword();

            assertEquals("Beheerderslogin", alert.getTitle());
            assertEquals("Toegang geweigerd", alert.getHeaderText());
            assertEquals("Onjuist wachtwoord! Probeer het opnieuw.", alert.getContentText());
        });

    }

//    @Test
//    public void testJoinCodeLength() {
//        Platform.runLater(() -> {
//            Alert alert = errorService.joinCodeLength("12345");
//            assertEquals("Uitnodigingscode is te kort", alert.getHeaderText());
//            assertEquals("Event code moet 6 karakters lang zijn. Probeer het opnieuw.",
//                alert.getContentText());
//        });
//    }

//    @Test
//    public void testJoinCodeLength2() {
//        Platform.runLater(() -> {
//            Alert alert = errorService.joinCodeLength("1234335");
//            assertEquals("Uitnodigingscode is te lang", alert.getHeaderText());
//            assertEquals("Event code moet 6 karakters lang zijn. Probeer het opnieuw.",
//                alert.getContentText());
//        });
//    }

    @Test
    public void testEventCodeNotFound() {
        Platform.runLater(() -> {
            Alert alert = errorService.eventCodeNotFound(new Exception("Bad Request"));
            assertEquals(
                "Uitnodigingscode niet gevonden. Controleer de code en probeer het opnieuw.",
                alert.getContentText());
        });
    }

//    @Test
//    public void testEventCodeNotFound2() {
//        Platform.runLater(() -> {
//            Alert alert = errorService.eventCodeNotFound(new Exception("Not Found"));
//            assertEquals("Er is een fout opgetreden. Probeer het later opnieuw.",
//                alert.getContentText());
//        });
//    }

    @Test
    public void testServerConnectionError() {
        Platform.runLater(() -> {
            Alert alert = errorService.serverConnectionError();
            assertEquals("Verbindingsfout", alert.getTitle());
            assertEquals(
                "De server waarmee je probeert te verbinden kan niet worden bereikt.",
                alert.getHeaderText());
        });
    }

    @Test
    public void testSuccessServerChange() {
        Platform.runLater(() -> {
            Alert alert = errorService.successServerChange("lol");
            assertEquals("Server-URL gewijzigd", alert.getTitle());
            assertEquals("Server-URL gewijzigd naar: lol", alert.getHeaderText());
            assertEquals(
                "Server-URL succesvol gewijzigd. Herstart de applicatie om de wijzigingen toe te passen.",
                alert.getContentText());
        });
    }

    @Test
    public void testWrongArgument() {
        Platform.runLater(() -> {
            Alert alert = errorService.wrongArgument("lol");
            assertEquals("Onjuiste invoer", alert.getTitle());
            assertEquals("lol", alert.getHeaderText());
            assertEquals("Probeer het opnieuw.", alert.getContentText());
        });
    }

    @Test
    public void testSomethingWrong() {
        Platform.runLater(() -> {
            Alert alert = errorService.somethingWrong();
            assertEquals("Server-URL niet gewijzigd", alert.getTitle());
            assertEquals("Er is iets misgegaan bij het wijzigen van de URL",
                alert.getHeaderText());
            assertEquals("URL is onjuist of server draait niet. Probeer het opnieuw.",
                alert.getContentText());
        });
    }

    @Test
    public void testConfirmDeleteEvent() {
        Platform.runLater(() -> {
            Alert alert = errorService.confirmDeleteEvent("lol");
            assertEquals("Evenement verwijderen", alert.getTitle());
            assertEquals(
                "Weet je zeker dat je dit evenement permanent uit de database wilt verwijderen?",
                alert.getHeaderText());
            assertEquals("Je staat op het punt het evenement met uitnodigingscode: lol te verwijderen?",
                alert.getContentText());
        });
    }

    @Test
    public void testGeneralError() {
        Platform.runLater(() -> {
            Alert alert = errorService.generalError("lol");
            assertEquals("lol", alert.getContentText());
        });
    }

    @Test
    public void testNumberFormatError() {
        Platform.runLater(() -> {
            Alert alert = errorService.numberFormatError();
            assertEquals("Voer een bedrag in", alert.getContentText());
        });
    }

//    @Test
//    public void testNoFirstName() {
//        Platform.runLater(() -> {
//            Alert alert = errorService.noFirstName();
//            assertEquals("Voornaam is verplicht om in te vullen", alert.getContentText());
//        });
//    }

    @Test
    public void testConfirmDeleteParticipant() {
        Participant p = new Participant("a", "b");
        Platform.runLater(() -> {
            Alert alert = errorService.confirmDeleteParticipant(p);
            assertEquals("Deelnemer verwijderen", alert.getTitle());
            assertEquals("Weet je zeker dat je deze deelnemer wilt verwijderen?",
                alert.getHeaderText());
//            assertEquals("""
//                    Voornaam: a
//                    Achternaam: b
//                    Email: null
//                    IBAN: null
//                    BIC: null""", alert.getContentText());
        });
    }

    @Test
    public void testConfirmDeletePayment() {
        Expense expense = new Expense();
        expense.setTitle("lol");
        expense.setAmount(1);
        LocalDate date = LocalDate.now();
        String d = date.toString();
        expense.setDate(date);
        Platform.runLater(() -> {
            Alert alert = errorService.confirmDelete(true, expense);
            assertEquals("Betaling verwijderen", alert.getTitle());
            assertEquals("Weet je zeker dat je het volgende wilt verwijderen: betaling?",
                alert.getHeaderText());
            assertEquals("""
                lol
                1.0
                """ + d , alert.getContentText());
        });
    }

    @Test
    public void testConfirmDeleteExpense() {
        Expense expense = new Expense();
        expense.setTitle("lol");
        expense.setAmount(1);
        LocalDate date = LocalDate.now();
        String d = date.toString();
        expense.setDate(date);
        Platform.runLater(() -> {
            Alert alert = errorService.confirmDelete(false, expense);
            assertEquals("Uitgave verwijderen", alert.getTitle());
            assertEquals("Weet je zeker dat je het volgende wilt verwijderen: uitgave?",
                alert.getHeaderText());
            assertEquals("lol" + "\n" + 1.0 + "\n" + d , alert.getContentText());
        });
    }

    @Test
    public void testCannotDelete() {
        Platform.runLater(() -> {
            Alert alert = errorService.cannotDelete("delete_expense", "no_expenses_to_delete");
            assertEquals("Uitgave verwijderen", alert.getTitle());
            assertEquals("Geen uitgaven om te verwijderen", alert.getHeaderText());
        });
    }
}