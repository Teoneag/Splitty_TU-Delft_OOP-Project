package client.scenes;

import client.services.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class SettingsCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ConfigService configService;
    private final I18NService i18NService;
    private final ErrorService errorService;
    private final CurrencyService currencyService;
    private final EmailService emailService;
    private final StyleService styleService;

    @FXML
    private Label settingsLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Button downloadButton;
    @FXML
    private Label themeLabel;
    @FXML
    private ChoiceBox<String> themeChoiceBox;
    @FXML
    private Label emailLabel;
    @FXML
    private TextField emailField;
    @FXML
    private Button changeEmailButton;
    @FXML
    private Label emailStatus;
    @FXML
    private Label serverLabel;
    @FXML
    private TextField serverURL;
    @FXML
    private Button changeServerButton;
    @FXML
    private Label currentServer;
    @FXML
    private Label serverNote;

    /**
     * Constructor for the SettingsOverviewCtrl class
     *
     * @param server        the server utility class
     * @param mainCtrl      the main controller class
     * @param configService the config service class
     * @param i18NService   the language service class
     * @param emailService  the email service class
     * @param errorService  the error service class
     */
    @Inject
    public SettingsCtrl(ServerUtils server, MainCtrl mainCtrl, ConfigService configService, I18NService i18NService, EmailService emailService, ErrorService errorService, CurrencyService currencyService, StyleService styleService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.configService = configService;
        this.i18NService = i18NService;
        this.errorService = errorService;
        this.emailService = emailService;
        this.currencyService = currencyService;
        this.styleService = styleService;
    }

    /**
     * initialize the settings screen with the language from the json file
     *
     * @param url            url
     * @param resourceBundle resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setLanguage();
        i18NService.setTranslation(themeChoiceBox, configService.getTheme());
    }

    /**
     * set the language of the settings screen
     */
    public void setLanguage() {
        i18NService.setTranslation(settingsLabel, "settings.title");
        i18NService.setTranslation(languageLabel, "language");
        i18NService.setTranslation(downloadButton, "download_language_template");
        i18NService.setTranslation(themeLabel, "theme");
        i18NService.setTranslation(themeChoiceBox, styleService.getThemes());
        i18NService.setTranslation(emailLabel, "email");
        i18NService.setTranslation(changeEmailButton, "change");
        i18NService.setTranslation(serverLabel, "server");
        i18NService.setTranslation(changeServerButton, "change");
        i18NService.setTranslation(currentServer, "current_server", configService.getServer());
        i18NService.setTranslation(serverNote, "format");
    }

    @FXML
    private void changeTheme() {
        if (themeChoiceBox.getSelectionModel().getSelectedIndex() == -1) return;
        String theme = styleService.getTheme(themeChoiceBox.getSelectionModel().getSelectedIndex());
        configService.setTheme(theme);
        i18NService.setTranslation(themeChoiceBox, configService.getTheme());
        mainCtrl.setTheme(theme);
    }

    /**
     * Downloads the language template (messages_en.properties) to a location chosen by the user.
     */
    @FXML
    public void downloadLangTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18NService.get("save.language.template"));
        fileChooser.setInitialFileName("messages_en.properties");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Properties files (*.properties)", "*.properties"));

        File file = fileChooser.showSaveDialog(null);
        if (file == null) {
            errorService.infoAlert("message", "no.file.selected", "");
            return;
        }
        try (InputStream in = getClass().getResourceAsStream("/messages_en.properties")) {
            if (in == null) {
                errorService.errorAlert("error", "file.not.found", "");
                return;
            }
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            errorService.errorAlert("error", "file.not.saved", "error.downloading");
        }
        errorService.infoAlert("message", "file.saved.successfully", "");
    }

    /**
     * save the url given by user to the config file
     * checks if the port is 8080, otherwise shows an error message
     */
    @FXML
    public void saveURL() {
        try {
            String url = serverURL.getText();
            String urlPattern = "^https?:\\/\\/(localhost|((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4})\\:\\d{1,5}\\/$";
            if (!url.matches(urlPattern)) {
                // ToDO show error message instead of exception
                throw new IllegalArgumentException(i18NService.get("url.not.correct.format"));
            }

            //save the url to the config file and show the current url
            configService.setServer(url);
            currentServer.setText(url);
            server.refreshServer();

            //show dialog which confirms the change
            Alert alert = errorService.successServerChange(url);
            alert.showAndWait();
        } catch (IllegalArgumentException e) {
            Alert alert = errorService.wrongArgument(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = errorService.somethingWrong();
            alert.showAndWait();
        }
    }

    /**
     * test if the email is sent
     */
    @FXML
    private void changeEmail() {
        i18NService.setTranslation(emailStatus, "sending.email");
        emailStatus.setTextFill(Color.ORANGE);
        configService.setEmail(emailField.getText());

        new Thread(() -> {
            if (!emailService.sendDefaultEmail()) {
                emailStatus.setTextFill(Color.RED);
                Platform.runLater(() -> styleService.playFadeTransition(emailStatus, "email.not.sent"));
            } else {
                emailStatus.setTextFill(Color.LIGHTGREEN);
                Platform.runLater(() -> styleService.playFadeTransition(emailStatus, "email.sent"));
            }
        }).start();
    }
}
