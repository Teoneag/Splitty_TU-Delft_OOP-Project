package client.scenes;

import client.services.ErrorService;
import client.services.EventService;
import client.services.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Tag;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.awt.*;
import java.net.ConnectException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;


public class AddEventCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final EventService eventService;
    private final ErrorService errorService;

    @FXML
    private TextField eventTitle;
    @FXML
    private Label titleErrorMessageAddEvent;

    @FXML
    private TextField eventDescription;
    @FXML
    private Label descriptionErrorMessageAddEvent;
    @FXML
    private Text eventTitleText;
    @FXML
    private Text title;
    @FXML
    private Text description;
    @FXML
    private Button createEventButton;
    @FXML
    private Button backButton;

    /**
     * Constructor for AddEventCtrl
     *
     * @param server       server
     * @param mainCtrl     mainCtrl
     * @param eventService eventService
     * @param errorService errorService
     */
    @Inject
    public AddEventCtrl(ServerUtils server, MainCtrl mainCtrl, EventService eventService, ErrorService errorService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.eventService = eventService;
        this.errorService = errorService;
    }

    /**
     * Initialize
     * @param url url
     * @param resourceBundle resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Platform.runLater(() -> eventTitle.requestFocus());

        eventTitle.textProperty().addListener((observable, oldValue, newValue) -> eventService.titleCheck(eventTitle, titleErrorMessageAddEvent));

        eventDescription.textProperty().addListener((observable, oldValue, newValue) -> eventService.descriptionCheck(eventDescription, descriptionErrorMessageAddEvent));
    }

    /**
     * Set the language of the AddEvent screen
     *
     * @param map the language map which contains the translation
     */
    public void setLanguage(HashMap<String, Object> map) {

        eventService.setLanguage(map);
        eventTitleText.setText((String) map.get("create_event_title"));
        title.setText((String) map.get("title"));
        description.setText((String) map.get("desc"));
        createEventButton.setText((String) map.get("create_buttonE"));
        backButton.setText((String) map.get("back"));
        eventTitle.setPromptText((String) map.get("title"));
        eventDescription.setPromptText((String) map.get("desc"));

        mainCtrl.setDynamicButtonSize(createEventButton);
        mainCtrl.setDynamicButtonSize(backButton);

        errorService.changeLanguage(map);
    }


    /**
     * creates an event with the information provided by the user and sends it to the server
     * after the event is created, the input fields are cleared by clearAddEventFields
     * also goes back to the main screen
     */
    public void createEvent() {
        try {
            if (!eventService.validateTitleAndDesc(eventTitle, titleErrorMessageAddEvent, eventDescription, descriptionErrorMessageAddEvent)) {
                return;
            }
            Event event = server.addEvent(eventTitle.getText(), eventDescription.getText());
            addBasicTags(event);
            clearAddEventFields();
            mainCtrl.showEventOverview(event.getInviteCode());
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        } catch (WebApplicationException e) {
            Alert alert = errorService.generalError(e.getMessage());
            alert.showAndWait();
            throw e;
        }
    }

    /**
     * adds basic tags to the event
     *
     * @param event the event to which the tags are added
     */
    public void addBasicTags(Event event) {
        Tag foodTag = new Tag("Food", new Color(0, 255, 0).getRGB(), event);
        Tag feesTag = new Tag("Entrance Fees", new Color(0, 0, 255).getRGB(), event);
        Tag travelTag = new Tag("Travel", new Color(255, 0, 0).getRGB(), event);
        Tag paymentTag = new Tag("Payment", Color.BLACK.getRGB(), event);

        server.addTag(foodTag);
        server.addTag(feesTag);
        server.addTag(travelTag);
        server.addTag(paymentTag);
    }

    /**
     * method that gets called by createEvent to clear the title and description input fields.
     */
    public void clearAddEventFields() {
        eventTitle.clear();
        eventDescription.clear();
        titleErrorMessageAddEvent.setText("");
        descriptionErrorMessageAddEvent.setText("");
    }

    /**
     * goes back to the main screen
     */
    public void goBack() {
        titleErrorMessageAddEvent.setText("");
        descriptionErrorMessageAddEvent.setText("");
        mainCtrl.showOverview();
    }

    /**
     * create a random invite code for event.
     * source: <a href="https://stackoverflow.com/questions/31213329/generation-of-referral-or-coupon-code">...</a>
     *
     * @return random code containing letters and numbers
     */
    public String createRandomCode(int codeLength) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
