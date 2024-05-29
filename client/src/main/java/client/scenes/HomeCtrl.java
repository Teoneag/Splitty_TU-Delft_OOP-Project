package client.scenes;

import client.services.*;
import com.google.inject.Inject;
import commons.Event;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;


public class HomeCtrl implements Initializable {
    private final ServerUtils server;
    private final StyleService styleService;
    private final MainCtrl mainCtrl;
    private final I18NService i18NService;
    private final ErrorService errorService;
    private final EventService eventService;
    private final CurrencyService currencyService;

    @FXML
    private TextField titleField;
    @FXML
    private Button createButton;
    @FXML
    private Label titleErrorLabel;
    @FXML
    private TextField codeField;
    @FXML
    private Button joinButton;
    @FXML
    private Label codeErrorLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label recentEventsLabel;
    @FXML
    private ListView<Event> eventsListView;

    /**
     * Constructor for the HomeOverviewCtrl class
     *
     * @param server       the server utility class
     * @param styleService the style utility class
     * @param mainCtrl     the main controller class
     * @param i18NService  the language service class
     * @param errorService the error service class
     * @param eventService the event service class
     */
    @Inject
    public HomeCtrl(ServerUtils server, StyleService styleService, MainCtrl mainCtrl, I18NService i18NService, ErrorService errorService, EventService eventService, CurrencyService currencyService) {
        this.server = server;
        this.styleService = styleService;
        this.mainCtrl = mainCtrl;
        this.i18NService = i18NService;
        this.errorService = errorService;
        this.eventService = eventService;
        this.currencyService = currencyService;
    }

    /**
     * initialize the home screen with the language from the json file
     *
     * @param url            the url
     * @param resourceBundle the resource bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setLanguage();
        errorService.bindTitleCheck(titleField, titleErrorLabel);
        errorService.bindCodeCheck(codeField, codeErrorLabel);
        errorService.bindPasswordCheck(passwordField, passwordErrorLabel);

        eventsListView.setPlaceholder(i18NService.labelFromKey("no_recent_events"));
        eventsListView.setCellFactory(param -> new ListCell<>() {
            private final HBox hBox = new HBox();
            private final Label text = new Label();
            private final Button eyeButton = new Button();

            {
                hBox.setAlignment(Pos.CENTER);
                hBox.getStyleClass().add("button");
                hBox.setPrefHeight(50);
                hBox.setFocusTraversable(true);
                eyeButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EYE));
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                hBox.getChildren().addAll(text, spacer, eyeButton);
                Tooltip.install(eyeButton, i18NService.tooltipFromKey("hide_event"));
                Tooltip.install(hBox, i18NService.tooltipFromKey("enter_event"));
                eyeButton.setOnAction(event -> {
                    Event evt = getItem();
                    if (evt != null) {
                        eventService.hideEvent(evt.getInviteCode());
                        eventsListView.getItems().remove(evt);
                    }
                    event.consume();
                });
                setOnMouseClicked(event -> {
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        Event evt = getItem();
                        if (evt != null) {
                            mainCtrl.showEvent(evt.getInviteCode());
                        }
                        event.consume();
                    }
                });
                hBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                        Event evt = getItem();
                        if (evt != null) {
                            mainCtrl.showEvent(evt.getInviteCode());
                        }
                        event.consume();
                    }
                });
            }

            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item.getTitle());
                    setGraphic(hBox);
                }
            }
        });
    }

    public void focusTitleField() {
        titleField.requestFocus();
    }

    /**
     * Set the language of the page
     */
    private void setLanguage() {
        i18NService.setTranslation(titleField, "event_title");
        i18NService.setTranslation(createButton, "create_event");
        i18NService.setTranslation(codeField, "event_code");
        i18NService.setTranslation(joinButton, "join");
        i18NService.setTranslation(passwordField, "password");
        i18NService.setTranslation(loginButton, "login_as_admin");
        i18NService.setTranslation(recentEventsLabel, "recent_events");
    }

    public void refresh() {
        updateRecentEvents();
    }

    private void updateRecentEvents() {
        eventsListView.setItems(FXCollections.observableArrayList(eventService.getRecentEvents()));
    }

    /**
     * creates an event with the information provided by the user and sends it to the server
     * after the event is created, the input fields are cleared by clearAddEventFields
     * also goes back to the main screen
     */
    @FXML
    private void createEvent() {
        try {
            if (!errorService.validateTitle(titleField, titleErrorLabel)) return;

            Event event = server.addEvent(titleField.getText(), "Description");
            titleField.clear();
            i18NService.setText(titleErrorLabel, "");
            mainCtrl.showEvent(event.getInviteCode());
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
     * when the user clicks the join event button, the application will show the event overview screen
     */
    @FXML
    private void joinEvent() {
        if (!errorService.validateCode(codeField, codeErrorLabel)) return;
        codeField.clear();
        i18NService.setText(codeErrorLabel, "");
        try {
            Event event = server.getEvent(codeField.getText());
            mainCtrl.showEvent(event.getInviteCode());
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        } catch (Exception e) {
            var alert = errorService.eventCodeNotFound(e);
            alert.showAndWait();
        }
    }

    /**
     * when the user clicks the "login admin" button, this method sends a post-request to the server
     * containing the filled-in password for an equality check.
     * if the password is correct, the application will show the admin overview screen,
     * else a pop-up will appear with the error message.
     */
    @FXML
    private void adminLogin() {
        if (!errorService.validatePassword(passwordField, passwordErrorLabel)) return;
        try {
            final String result = server.checkAdminPassword(passwordField.getText());
            if (result.equals("Admin password correct!")) mainCtrl.showAdminOverview();
            else errorService.wrongPassword().showAndWait();
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) mainCtrl.serverConnectionAlert();
            else throw e;
        }
    }
}
