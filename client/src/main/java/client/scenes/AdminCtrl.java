package client.scenes;

import client.services.*;
import com.google.inject.Inject;
import commons.Event;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;


public class AdminCtrl implements Initializable {
    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private final I18NService i18NService;
    private final ErrorService errorService;
    private final StyleService styleService;
    private final EventService eventService;

    private ObservableList<Event> data;

    @FXML
    private Label adminOverviewLabel;
    @FXML
    private Label copiedLabel;
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
    private Button deleteButton;
    @FXML
    private Button downloadButton;
    @FXML
    private Button refreshButton;

    /**
     * Constructor for AdminOverviewCtrl
     *
     * @param server       server
     * @param mainCtrl     mainCtrl
     * @param i18NService  languageService
     * @param errorService errorService
     */
    @Inject
    public AdminCtrl(ServerUtils server, MainCtrl mainCtrl, I18NService i18NService, ErrorService errorService,
                     StyleService styleService, EventService eventService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.i18NService = i18NService;
        this.errorService = errorService;
        this.styleService = styleService;
        this.eventService = eventService;
    }

    /**
     * Initialize
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setLanguage();
        addEventListener();
        colInviteCode.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().getInviteCode())));
        colTitle.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getTitle()));
        colDescription.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getDescription()));
        colCreationDate.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getCreationDate().toString()));
        colLastActivity.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getLastModified().toString()));
    }

    /**
     * Set the language of the labels and buttons
     */
    public void setLanguage() {
        i18NService.setTranslation(adminOverviewLabel, "admin.overview");
        i18NService.setTranslation(copiedLabel, "invite.code.copied");
        i18NService.setTranslation(colInviteCode, "invite.code");
        i18NService.setTranslation(colTitle, "title");
        i18NService.setTranslation(colDescription, "description");
        i18NService.setTranslation(colCreationDate, "creation.date");
        i18NService.setTranslation(colLastActivity, "last.activity");
        i18NService.setTranslation(importButton, "import.event");
        i18NService.setTranslation(deleteButton, "delete");
        i18NService.setTranslation(downloadButton, "download.event");
        i18NService.setTranslation(refreshButton, "refresh");
    }

    /***
     * Add an event listener to the table for each row
     */
    public void addEventListener() {
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int rowIndex = table.getSelectionModel().getSelectedIndex();
                if (rowIndex >= 0) {
                    String name = table.getItems().get(rowIndex).getInviteCode();
                    styleService.copyInviteCode(copiedLabel, name);
                }
            }
        });
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
        server.longPollingRegisterEvent(e -> data.setAll(e));
    }

    public void stop() {
        server.stop();
    }

    /**
     * Import an event from a json file
     */
    public void importEvent() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18NService.get("import.event"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            errorService.infoAlert("no.file.selected", "message", "");
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
            errorService.errorAlert("file.not.found", "error", "");
            return;
        }
        refresh();
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
            eventService.hideEvent(selected.getInviteCode());
            try (Response response = server.deleteEvent(selected)) {
                if (response.getStatus() != 200) {
                    errorService.errorAlert("delete.error", "error", "");
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
            fileChooser.setTitle(i18NService.get("save.event"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            fileChooser.setInitialFileName(selected.getInviteCode() + ".json");
            File file = fileChooser.showSaveDialog(null);
            if (file == null) {
                errorService.infoAlert("no.file.selected", "message", "");
                return;
            }
            boolean res = file.createNewFile();
            if (!res) {
                errorService.errorAlert("file.exists", "error", "");
                return;
            }
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.print(jsonEvent);
            } catch (FileNotFoundException e) {
                errorService.errorAlert("file.not.found", "error", "");
            }
        } else {
            errorService.infoAlert("no.event.selected", "message", "");
        }
    }
}
