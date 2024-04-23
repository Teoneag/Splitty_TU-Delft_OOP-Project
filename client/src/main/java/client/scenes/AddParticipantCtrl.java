package client.scenes;

import client.services.ConfigService;
import client.services.EmailService;
import client.services.ErrorService;
import client.services.ServerUtils;
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
import java.util.HashMap;


public class AddParticipantCtrl implements Initializable {
    private final ServerUtils server;
    private final EmailService emailService;
    private final ConfigService configService;
    private final MainCtrl mainCtrl;
    private Event event;
    private Participant participant;
    private final ErrorService errorService;

    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField email;
    @FXML
    private TextField iban;
    @FXML
    private TextField bic;
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label ibanLabel;
    @FXML
    private Label bicLabel;
    @FXML
    private Button submitButton;
    @FXML
    private Button backButton;
    
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
     * initialize the add participant screen with the language from the json file
     *
     * @param location the location
     * @param resources the resources
     */
    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {

    }

    /**
     * Set the language of the labels and buttons
     *
     * @param map the language map which contains the translation
     */
    public void setLanguage(HashMap<String, Object> map) {
        firstNameLabel.setText((String) map.get("first_name"));
        lastNameLabel.setText((String) map.get("last_name"));
        emailLabel.setText((String) map.get("email"));
        ibanLabel.setText((String) map.get("iban"));
        bicLabel.setText((String) map.get("bic"));
        submitButton.setText((String) map.get("submit"));
        backButton.setText((String) map.get("backToEvent"));
        
        firstNameErrorLabel.setText(map.get("first_name").toString() +  map.get("tooLong"));
        lastNameErrorLabel.setText(map.get("last_name").toString() +  map.get("tooLong"));
        emailErrorLabel.setText(map.get("email").toString() +  map.get("tooLong"));
        ibanErrorLabel.setText(map.get("iban").toString() +  map.get("tooLong"));
        bicErrorLabel.setText(map.get("bic").toString() + map.get("tooLong"));
        
        errorService.changeLanguage(map);

        mainCtrl.setDynamicButtonSize(submitButton);
        mainCtrl.setDynamicButtonSize(backButton);
    }

    /**
     * Injectable constructor
     *
     * @param server   server
     * @param emailService emailService
     * @param mainCtrl mainCtrl
     * @param event    parent event
     * @param configService configService
     * @param errorService errorService
     */
    @Inject
    public AddParticipantCtrl(ServerUtils server, MainCtrl mainCtrl, Event event,
                              EmailService emailService, ConfigService configService, ErrorService errorService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.emailService = emailService;
        this.configService = configService;
        this.errorService = errorService;
    }

    /**
     * Should show the event from which the event was called
     */
    public void goBack() {
        resetFields();
        mainCtrl.showEventOverview(event);
    }

    /**
     * Submits the created participant to the server
     */
    public void submitParticipant() {
        if (firstName.getText().isBlank()) {
            Alert alert = errorService.noFirstName();
            alert.showAndWait();
        }
        
        if (doesNotSatisfyTextRequirements()) return;
        
        String prevMail = participant.getEmail();

        participant.setFirstName(firstName.getText());
        participant.setLastName(lastName.getText());
        participant.setEmail(email.getText());
        participant.setIban(iban.getText());
        participant.setBic(bic.getText());
        participant.setEventInviteCode(event.getInviteCode());

        String currentMail = participant.getEmail();
        new Thread(() -> {
            if(!currentMail.equals(prevMail)
                    && !currentMail.equals(configService.getEmail())
                    && server.getParticipantsByEventInviteCode(event.getInviteCode())
                            .stream()
                            .map(Participant::getEmail)
                            .noneMatch(currentMail::equals)) {

                if (emailService.sendInviteEmail(participant.getEmail(), event.getInviteCode(),
                        participant.getFirstName())) {
                    // ToDo: Show Invite email sent
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
    
    /**
     * Checks that the length of the text is not too long and sets the error message visible or not
     * @return returns true if there is a field that violates the length condition
     */
    private boolean doesNotSatisfyTextRequirements() {

        firstNameErrorLabel.setVisible(firstName.getText().length() > 255);
        lastNameErrorLabel.setVisible(lastName.getText().length() > 255);
        emailErrorLabel.setVisible(email.getText().length() > 255);
        ibanErrorLabel.setVisible(iban.getText().length() > 255);
        bicErrorLabel.setVisible(bic.getText().length() > 255);

        return (firstName.getText().isBlank() || firstName.getText().length() > 255 || lastName.getText().length() > 255
            || email.getText().length() > 255 || iban.getText().length() > 255 || bic.getText().length() > 255);
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
        return new Participant(
            firstName.getText(),
            lastName.getText(),
            email.getText(),
            iban.getText(),
            bic.getText(),
            event.getInviteCode()
        );
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
        firstName.setText(participant.getFirstName());
        lastName.setText(participant.getLastName());
        email.setText(participant.getEmail());
        iban.setText(participant.getIban());
        bic.setText(participant.getBic());
    }

    /**
     * Clears the field of the fxml file
     */
    public void resetFields() {
        firstNameErrorLabel.setVisible(false);
        lastNameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        ibanErrorLabel.setVisible(false);
        bicErrorLabel.setVisible(false);

        firstName.clear();
        lastName.clear();
        email.clear();
        iban.clear();
        bic.clear();
    }
}
