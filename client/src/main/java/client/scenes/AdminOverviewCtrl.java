package client.scenes;

import client.services.ConfigService;
import client.services.ErrorService;
import client.services.LanguageService;
import client.services.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Scanner;


public class AdminOverviewCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LanguageService languageService;
    private final ConfigService configService;
    private final ErrorService errorService;

    private ObservableList<Event> data;
    private HashMap<String, Object> map;
    @FXML
    private TableView<Event> table;
    @FXML
    private TableColumn<Event, String> colInviteCode;
    @FXML
    private TableColumn<Event, String> colTitle;
    @FXML
    private TableColumn<Event, String> colDescription;
    @FXML
    private TableColumn<Event, String> colCreationDate;
    @FXML
    private TableColumn<Event, String> colLastActivity;
    @FXML
    private Button importButton;
    @FXML
    private Button downloadButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button backButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Label fadeLabel;
    @FXML
    private ComboBox<String> languageBox;
    @FXML
    private Text adminOverviewText;

    /**
     * Constructor for AdminOverviewCtrl
     * @param server server
     * @param mainCtrl mainCtrl
     * @param languageService languageService
     * @param configService configService
     * @param errorService errorService
     */
    @Inject
    public AdminOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, LanguageService languageService,
                             ConfigService configService, ErrorService errorService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.languageService = languageService;
        this.configService = configService;
        this.errorService = errorService;
        this.map = configService.getLanguage();
    }

    /**
     * Initialize
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageService.setLanguagesComboBox(languageBox);
        addEventlistener();
        colInviteCode.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().getInviteCode())));
        colTitle.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getTitle()));
        colDescription.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getDescription()));
        colCreationDate.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getCreationDate().toString()));
        colLastActivity.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getLastModified().toString()));


    }

    /**
     * change the language of the application without restarting it
     */
    @FXML
    public void changeLang() {
        languageService.changeLanguage(mainCtrl, languageBox);
    }

    /**
     * Set the language of the labels and buttons
     *
     * @param map      the language map which contains the translation
     * @param language the language to set
     */
    public void setLanguage(HashMap<String, Object> map, String language) {
        colTitle.setText((String) map.get("colTitle"));
        colCreationDate.setText((String) map.get("colCreationDate"));
        colLastActivity.setText((String) map.get("colLastActivity"));
        colInviteCode.setText((String) map.get("colInviteCode"));
        colDescription.setText((String) map.get("colDescription"));
        fadeLabel.setText((String) map.get("copiedToClipboard"));

        importButton.setText((String) map.get("import"));
        downloadButton.setText((String) map.get("download"));
        deleteButton.setText((String) map.get("delete"));
        backButton.setText((String) map.get("backToMain"));
        refreshButton.setText((String) map.get("refresh"));
        adminOverviewText.setText((String) map.get("adminOverviewText"));

        this.map = map;
        errorService.changeLanguage(map);

        mainCtrl.setDynamicButtonSize(importButton);
        mainCtrl.setDynamicButtonSize(downloadButton);
        mainCtrl.setDynamicButtonSize(deleteButton);
        mainCtrl.setDynamicButtonSize(backButton);
        mainCtrl.setDynamicButtonSize(refreshButton);

        languageBox.setValue(language);
    }

    /***
     * Add an event listener to the table for each row
     */
    public void addEventlistener() {
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int rowIndex = table.getSelectionModel().getSelectedIndex();
                if (rowIndex >= 0) {
                    //get invite code of the selected event
                    String name = table.getItems().get(rowIndex).getInviteCode();
                    copyToClipboard(name);
                    showFadeLabel();
                }
            }
        });
    }

    /**
     * shows a label when an invitecode of an event is copied to the clipboard
     * label will fade out after 2 seconds
     */
    public void showFadeLabel() {
        fadeLabel.setOpacity(1.0);
        fadeLabel.setVisible(true); // Make the label visible

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), fadeLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
    }

    /**
     * gets the invitecode of the selected row from the table and copies it to the clipboard
     *
     * @param str invite to copy and set to clipboard
     */
    public void copyToClipboard(String str) {
        java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(str);
        java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    /**
     * Refresh the table
     */
    public void refresh() {
        try {
            data = FXCollections.observableList(server.getEvents());
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }

        table.setItems(data);
        server.longPollingRegisterEvent(e -> {
            data.setAll(e);
        });
    }

    public void stop() {
        server.stop();
    }

    /**
     * Ask the user to confirm the deletion of an event after selecting ok it will delete the event from the database
     */
    public void deleteEvent() {
        var selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = errorService.confirmDeleteEvent(selected.getInviteCode());
            alert.showAndWait();
            if (alert.getResult() != ButtonType.OK) {
                return;
            }
            try (Response response = server.deleteEvent(selected)) {
                if (response.getStatus() != 200) {
                    Popup popup = new Popup();
                    popup.getContent().add(
                            new javafx.scene.control.Label(map.get("delError").toString() + response.getStatus()));
                    popup.show(null);
                }
            } catch (Exception e) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            refresh();
        }
    }

    /**
     * Export an event to a json file
     */
    public void exportEvent() throws IOException {
        var selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String jsonEvent;
            try {
                jsonEvent = server.getJsonEvent(selected.getInviteCode());
            } catch (ProcessingException e) {
                if (e.getCause().getClass() == ConnectException.class) {
                    mainCtrl.serverConnectionAlert();
                    return;
                }
                throw e;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(map.get("saveEvent").toString());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            fileChooser.setInitialFileName(selected.getInviteCode() + ".json");
            File file = fileChooser.showSaveDialog(null);
            if (file == null) {
                // ToDo show map.get("noFileSelected").toString()
                return;
            }
            file.createNewFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.print(jsonEvent);
            } catch (FileNotFoundException e) {
                // ToDo show map.get("fileNotFound").toString()
            }
        } else {
            // ToDo show map.get("noEventSelected").toString()
        }
    }

    /**
     * Import an event from a json file
     */
    public void importEvent() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(map.get("resEvent").toString());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            // ToDo show map.get("noFileSelected").toString()
            return;
        }
        StringBuilder jsonEvent = new StringBuilder();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                jsonEvent.append(" ");
                jsonEvent.append(scanner.next());
            }
            server.addJsonEvent(jsonEvent.toString());
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        } catch (FileNotFoundException e) {
            // ToDo show map.get("fileNotFound").toString()
            return;
        }
        refresh();
    }

    public void goBack() {
        mainCtrl.showOverview();
    }
}
