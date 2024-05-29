package client.scenes;

import client.services.*;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.ConnectException;


public class AddParticipantCtrl implements Initializable {
    private final ServerUtils server;
    private final EmailService emailService;
    private final ConfigService configService;
    private final MainCtrl mainCtrl;

    private Event event;
    private Participant participant;
    private final ErrorService errorService;
    private final I18NService i18NService;

    @FXML
    private Label addParticipantLabel;
    @FXML
    private Label firstNameLabel;
    @FXML
    private TextField firstNameField;
    @FXML
    private Label lastNameLabel;
    @FXML
    private TextField lastNameField;
    @FXML
    private Label emailLabel;
    @FXML
    private TextField emailField;
    @FXML
    private Label ibanLabel;
    @FXML
    private TextField ibanField;
    @FXML
    private Label bicLabel;
    @FXML
    private TextField bicField;
    @FXML
    private Button submitButton;

    @FXML
    private Label firstNameErrorLabel;
    @FXML
    private Label lastNameErrorLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label ibanErrorLabel;
    @FXML
    private Label bicErrorLabel;

    /**
     * Injectable constructor
     *
     * @param server        server
     * @param emailService  emailService
     * @param mainCtrl      mainCtrl
     * @param event         parent event
     * @param configService configService
     * @param errorService  errorService
     */
    @Inject
    public AddParticipantCtrl(ServerUtils server, MainCtrl mainCtrl, Event event, EmailService emailService, ConfigService configService, ErrorService errorService, I18NService i18NService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.emailService = emailService;
        this.configService = configService;
        this.errorService = errorService;
        this.i18NService = i18NService;
    }

    /**
     * initialize the add participant screen with the language from the json file
     *
     * @param location  the location
     * @param resources the resources
     */
    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        setLanguage();
        errorService.bindFirstNameCheck(firstNameField, firstNameErrorLabel);
        errorService.bindGenericCheck(lastNameField, lastNameErrorLabel);
        errorService.bindGenericCheck(emailField, emailErrorLabel);
        errorService.bindGenericCheck(ibanField, ibanErrorLabel);
        errorService.bindGenericCheck(bicField, bicErrorLabel);
    }

    /**
     * Set the language of the labels and buttons
     */
    public void setLanguage() {
        i18NService.setTranslation(addParticipantLabel, "add.participant");
        i18NService.setTranslation(firstNameLabel, "first.name");
        i18NService.setTranslation(lastNameLabel, "last.name");
        i18NService.setTranslation(emailLabel, "email");
        i18NService.setTranslation(ibanLabel, "iban");
        i18NService.setTranslation(bicLabel, "bic");
        i18NService.setTranslation(submitButton, "submit");
    }

    /**
     * Should show the event from which the event was called
     */
    public void goBack() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        ibanField.clear();
        bicField.clear();
        mainCtrl.showEvent(event);
    }

    /**
     * Submits the created participant to the server
     */
    public void handleSubmit() {
        if (!errorService.validateFirstName(firstNameField, firstNameErrorLabel)) return;

        String prevMail = participant.getEmail();

        participant.setFirstName(firstNameField.getText());
        participant.setLastName(lastNameField.getText());
        participant.setEmail(emailField.getText());
        participant.setIban(ibanField.getText());
        participant.setBic(bicField.getText());
        participant.setEventInviteCode(event.getInviteCode());

        String currentMail = participant.getEmail();
        new Thread(() -> {
            if (!currentMail.equals(prevMail) && !currentMail.equals(configService.getEmail()) && server.getParticipantsByEventInviteCode(event.getInviteCode()).stream().map(Participant::getEmail).noneMatch(currentMail::equals)) {

                if (emailService.sendInviteEmail(participant.getEmail(), event.getInviteCode(), participant.getFirstName())) {
                    mainCtrl.email = participant.getEmail();
                    // ToDo: Show Email sent
                } else {
                    // ToDo: Show No email sent
                }
            }
        }).start();

        try {
            if (this.participant.getId() != 0) {
                server.updateParticipant(participant);
            } else {
                server.addParticipant(participant);
            }

        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        } catch (WebApplicationException e) {
            Alert alert = errorService.generalError(e.getMessage());
            alert.showAndWait();
            return;
        }

        goBack();

    }

    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Creates a Participant from the filled in fields by the user
     *
     * @return - the Participant created
     */
    public Participant getParticipant() {
        return new Participant(firstNameField.getText(), lastNameField.getText(), emailField.getText(), ibanField.getText(), bicField.getText(), event.getInviteCode());
    }

    public void addParticipant(Event event) {
        this.event = event;
        this.participant = new Participant();
    }

    /**
     * Sets the values that already exist for the participant being edited
     *
     * @param event       the event of the participant
     * @param participant the participant being edited
     */
    public void edit(Event event, Participant participant) {
        this.event = event;
        this.participant = participant;
        firstNameField.setText(participant.getFirstName());
        lastNameField.setText(participant.getLastName());
        emailField.setText(participant.getEmail());
        ibanField.setText(participant.getIban());
        bicField.setText(participant.getBic());
    }
}
