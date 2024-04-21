package client.scenes;

import client.services.*;
import com.google.inject.Inject;
import commons.Event;
import jakarta.ws.rs.ProcessingException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;


public class HomeOverviewCtrl implements Initializable {
    private final ServerUtils server;
    private final StyleUtils style;
    private final MainCtrl mainCtrl;
    private final LanguageService languageService;
    private final ErrorService errorService;
    private final EventService eventService;
    @FXML
    private TextField joinCode;
    @FXML
    private PasswordField adminPassword;
    @FXML
    private Button createEventButton;
    @FXML
    private Button joinEventButton;
    @FXML
    private Button adminLoginButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button helpButton;
    @FXML
    private TableView<Event> recentEventsTable;
    @FXML
    private ComboBox<String> languageBox;
    @FXML
    private TableColumn<Event, String> colRecentEvents;
    @FXML
    private ImageView logoContainer;

    /**
     * Constructor for the HomeOverviewCtrl class
     *
     * @param server          the server utility class
     * @param style           the style utility class
     * @param mainCtrl        the main controller class
     * @param languageService the language service class
     * @param errorService    the error service class
     * @param eventService    the event service class
     */
    @Inject
    public HomeOverviewCtrl(ServerUtils server, StyleUtils style, MainCtrl mainCtrl,
                            LanguageService languageService, ErrorService errorService, EventService eventService) {
        this.server = server;
        this.style = style;
        this.mainCtrl = mainCtrl;
        this.languageService = languageService;
        this.errorService = errorService;
        this.eventService = eventService;
    }

    /**
     * initialize the home screen with the language from the json file
     *
     * @param url            the url
     * @param resourceBundle the resource bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            logoContainer.setImage(new Image("images/Splitty_Logo2.png"));
            languageService.setLanguagesComboBox(languageBox);
            colRecentEvents.setCellValueFactory(
                    p -> new ReadOnlyObjectWrapper<>(p.getValue().getTitle()));

            recentEventsTable.setFocusTraversable(true);

            recentEventsTable.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    Event selectedItem = recentEventsTable.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        mainCtrl.showEventOverview(selectedItem.getInviteCode());
                    }
                    event.consume(); // Consume the event to prevent further propagation
                }
            });

            recentEventsTable.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Event selectedItem = recentEventsTable.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        mainCtrl.showEventOverview(selectedItem.getInviteCode());
                    }
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * change the language of the application without restarting it
     */
    @FXML
    public void changeLang() {
        languageService.changeLanguage(mainCtrl, languageBox);
    }

    /**
     * Set the language of the page
     *
     * @param map      the language map which contains the translation
     * @param language the language to be set
     */
    public void setLanguage(HashMap<String, Object> map, String language) {
        createEventButton.setText((String) map.get("create_buttonH"));
        adminPassword.setPromptText((String) map.get("enter_password"));
        joinEventButton.setText((String) map.get("join"));
        adminLoginButton.setText((String) map.get("loginAdmin"));
        colRecentEvents.setText((String) map.get("recent_events"));
        settingsButton.setText((String) map.get("settingsHome"));
        joinCode.setPromptText((String) map.get("eventCode"));
        helpButton.setText((String) map.get("helpButton"));

        mainCtrl.setDynamicButtonSize(createEventButton);
        mainCtrl.setDynamicButtonSize(joinEventButton);
        mainCtrl.setDynamicButtonSize(adminLoginButton);
        mainCtrl.setDynamicButtonSize(settingsButton);
        mainCtrl.setDynamicButtonSize(helpButton);

        languageBox.setValue(language);
        errorService.changeLanguage(map);
    }

    public void refresh() {
        addRecentEventToTable();
        style.computeTableInsets(recentEventsTable);
    }

    public void openSettings() {
        mainCtrl.showSettingsOverview();
    }

    /**
     * Method that retrieves all events created in a certain server and adds them to a list.
     * The recentEvents table then gets cleared of all input data
     * and gets repopulated with the new and updated list of events
     */
    public void addRecentEventToTable() {
        try {
            List<Event> recentEvents = server.getEvents();
            List<String> recentEventsCodes = eventService.getRecentEvents();
            recentEvents.removeIf(event -> !recentEventsCodes.contains(event.getInviteCode()));
            recentEventsTable.getItems().clear();
            recentEventsTable.getItems().addAll(recentEvents);
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }
    }

    /**
     * when the user clicks the "create event" button,
     * the application will show the add event screen
     */
    @FXML
    public void createEventMainMenu() {
        mainCtrl.showAddEvent();
    }

    @FXML
    public void showShortcuts() {
        mainCtrl.showShortcuts();
    }

    /**
     * when the user clicks the "login admin" button, this method sends a post-request to the server
     * containing the filled-in password for an equality check.
     * if the password is correct, the application will show the admin overview screen,
     * else a pop-up will appear with the error message.
     */
    public void adminLogin() {
        //get user-input and send it to server for equality check
        var userInput = adminPassword.getText();
        String result;
        try {
            result = server.checkAdminPassword(userInput);
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }

        if (result.equals("Admin password correct!")) {
            //show admin overview
            mainCtrl.showAdminOverview();
        } else {
            //pop-up window telling that password is incorrect
            Alert alert = errorService.wrongPassword(result);
            alert.showAndWait();
        }
    }

    /**
     * when the user clicks the join event button, the application will show the event overview screen
     */
    @FXML
    public void joinEventMainMenu() {

        String inviteCode = joinCode.getText();

        if (inviteCode.length() != 6) {
            var alert = errorService.joinCodeLength(inviteCode);
            alert.showAndWait();
            return;
        }

        try {
            Event event = server.getEvent(inviteCode);

            mainCtrl.showEventOverview(event.getInviteCode());
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


}
