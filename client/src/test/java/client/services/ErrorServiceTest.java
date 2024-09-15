package client.services;


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

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
class ErrorServiceTest {

    @InjectMocks
    private ConfigService configService;
    @InjectMocks
    private ErrorService errorService;
    @InjectMocks
    private I18NService i18NService;
    @InjectMocks
    private HashMap<String, Object> map;
    @Mock
    private Alert alert;

    @BeforeEach
    public void setup() {
        map = configService.readJsonToMap("src/main/java/client/languages/Messages_nl.json");
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

            assertEquals("Admin Login", alert.getTitle());
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
                "De uitnodigingscode is niet gevonden. Controleer de code en probeer het opnieuw.",
                alert.getContentText());
        });
    }

    @Test
    public void testEventCodeNotFound2() {
        Platform.runLater(() -> {
            Alert alert = errorService.eventCodeNotFound(new Exception("Not Found"));
            assertEquals("Er is een fout opgetreden. Probeer het later opnieuw.",
                alert.getContentText());
        });
    }

    @Test
    public void testServerConnectionError() {
        Platform.runLater(() -> {
            Alert alert = errorService.serverConnectionError();
            assertEquals("Verbindingsfout", alert.getTitle());
            assertEquals(
                "De server waarmee je wil verbinden is niet bereikbaar. \nProbeer het later opnieuw of verander de url in de instellingen.",
                alert.getHeaderText());
        });
    }

    @Test
    public void testSuccessServerChange() {
        Platform.runLater(() -> {
            Alert alert = errorService.successServerChange("lol");
            assertEquals("Server URL aangepast", alert.getTitle());
            assertEquals("Server URL is aangepast naar: lol", alert.getHeaderText());
            assertEquals(
                "De server URL is succesvol aangepast. \nHerstart de applicatie om de wijzigingen door te voeren.",
                alert.getContentText());
        });
    }

    @Test
    public void testWrongArgument() {
        Platform.runLater(() -> {
            Alert alert = errorService.wrongArgument("lol");
            assertEquals("Foute invoer", alert.getTitle());
            assertEquals("lol", alert.getHeaderText());
            assertEquals("Probeer het opnieuw alsjeblieft.", alert.getContentText());
        });
    }

    @Test
    public void testSomethingWrong() {
        Platform.runLater(() -> {
            Alert alert = errorService.somethingWrong();
            assertEquals("Server URL is niet gewijzigd", alert.getTitle());
            assertEquals("Er ging iets fout tijdens het veranderen van de URL",
                alert.getHeaderText());
            assertEquals("URL is fout of server is niet beschikbaar. Probeer het opnieuw.",
                alert.getContentText());
        });
    }

    @Test
    public void testConfirmDeleteEvent() {
        Platform.runLater(() -> {
            Alert alert = errorService.confirmDeleteEvent("lol");
            assertEquals("Verwijder event", alert.getTitle());
            assertEquals(
                "Weet je zeker dat je dit event permanent wilt verwijderen uit de database?",
                alert.getHeaderText());
            assertEquals("Je gaat de event verwijderen met invitecode: lol",
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
            assertEquals("Vul een getal in.", alert.getContentText());
        });
    }

    @Test
    public void testNoFirstName() {
        Platform.runLater(() -> {
            Alert alert = errorService.noFirstName();
            assertEquals("Voornaam is verplicht om in te vullen", alert.getContentText());
        });
    }

    @Test
    public void testConfirmDeleteParticipant() {
        Participant p = new Participant("a", "b");
        Platform.runLater(() -> {
            Alert alert = errorService.confirmDeleteParticipant(p);
            assertEquals("Verwijder deelnemer", alert.getTitle());
            assertEquals("Weet je zeker dat je deze deelnemer wilt verwijderen?",
                alert.getHeaderText());
            assertEquals("""
                    Voornaam: a
                    Achternaam: b
                    Email: null
                    IBAN: null
                    BIC: null""", alert.getContentText());
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
            assertEquals("Verwijder betaling", alert.getTitle());
            assertEquals("Weet je zeker dat je het volgende wilt verwijderen: Betaling?",
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
            assertEquals("Verwijder uitgave", alert.getTitle());
            assertEquals("Weet je zeker dat je het volgende wilt verwijderen: Uitgave?",
                alert.getHeaderText());
            assertEquals("lol" + "\n" + 1.0 + "\n" + d , alert.getContentText());
        });
    }

    @Test
    public void testCannotDelete() {
        Platform.runLater(() -> {
            Alert alert = errorService.cannotDelete("delete_expense", "no_expenses_to_delete");
            assertEquals("Verwijder uitgave", alert.getTitle());
            assertEquals("Geen uitgaven om te verwijderen", alert.getHeaderText());
        });
    }
}