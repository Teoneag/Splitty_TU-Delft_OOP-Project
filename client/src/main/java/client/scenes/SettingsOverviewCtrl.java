package client.scenes;

import client.services.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SettingsOverviewCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ConfigService configService;
    private final LanguageService languageService;
    private final ErrorService errorService;
    private final Map<String, Object> statusAniMap = new LinkedHashMap<>();
    private final EmailService emailService;
    private HashMap<String, Object> languageMap;
    @FXML
    private Label settingsLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label currencyLabel;
    @FXML
    private Label themeLabel;
    @FXML
    private Label testEmailLabel;
    @FXML
    private Label emailStatus;
    @FXML
    private Button backButton;
    @FXML
    private Button changeButton;
    @FXML
    private Button downloadButton;
    @FXML
    private Button testMailButton;
    @FXML
    private ComboBox<String> currency;
    @FXML
    private Label serverNote;
    @FXML
    private Label serverNote2;
    @FXML
    private Label currentServer;
    @FXML
    private TextField serverURL;
    @FXML
    private ComboBox<String> languageBox;
    @FXML
    private ComboBox<String> themeSwitch;

    /**
     * Constructor for the SettingsOverviewCtrl class
     *
     * @param server          the server utility class
     * @param mainCtrl        the main controller class
     * @param configService   the config service class
     * @param languageService the language service class
     * @param emailService    the email service class
     * @param errorService    the error service class
     */
    @Inject
    public SettingsOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, ConfigService configService,
                                LanguageService languageService, EmailService emailService, ErrorService errorService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.configService = configService;
        this.languageService = languageService;
        this.errorService = errorService;
        this.emailService = emailService;
    }

    /**
     * initialize the settings screen with the language from the json file
     *
     * @param url            url
     * @param resourceBundle resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currency.getItems().addAll("EUR", "USD", "CHF", "GBP");
        currency.setValue(configService.getConfigCurrency());
        currency.valueProperty().addListener((obs, oldVal, newVal) -> {
            configService.setConfigCurrency(newVal);
        });
        languageService.setLanguagesComboBox(languageBox);
        themeSwitch.getItems().addAll("normal", "contrast");
        this.languageMap = configService.getLanguage();
        emailStatus.setOpacity(0.0);
    }

    @FXML
    public void setThemeSwitch() {
        String choice = themeSwitch.getValue();
        mainCtrl.setTheme(choice);
    }

    @FXML
    public void downloadLangTemplate() {
        languageService.downloadLangTemplate();
    }

    /**
     * change the language of the application without restarting it
     */
    @FXML
    public void changeLang() {
        languageService.changeLanguage(mainCtrl, languageBox);
    }

    /**
     * set the language of the settings screen
     *
     * @param map      - the language map which contains the translation
     * @param language - the language to be set
     */
    public void setLanguage(HashMap<String, Object> map, String language) {
        settingsLabel.setText((String) map.get("settings"));
        backButton.setText((String) map.get("backHome"));
        languageLabel.setText((String) map.get("language"));
        changeButton.setText((String) map.get("changeButton"));
        serverNote2.setText((String) map.get("currentServer"));
        serverNote.setText((String) map.get("format"));
        downloadButton.setText((String) map.get("downloadButton"));
        currencyLabel.setText((String) map.get("currency"));
        themeLabel.setText((String) map.get("theme"));

        this.languageMap = map;

        testMailButton.setText((String) map.get("testEmailButton"));
        statusAniMap.put("success", map.get("emailSent"));
        statusAniMap.put("fail", map.get("emailNotSent"));
        statusAniMap.put("wait", map.get("waitingMail"));

        mainCtrl.setDynamicButtonSize(backButton);
        mainCtrl.setDynamicButtonSize(changeButton);
        mainCtrl.setDynamicButtonSize(downloadButton);
        currentServer.setText(configService.getServer());

        languageBox.setValue(language);
        errorService.changeLanguage(map);
    }

    /**
     * save the url given by user to the config file
     * checks if the port is 8080, otherwise shows an error message
     */
    @FXML
    public void saveURL() {
        try {
            String url = serverURL.getText();
            String urlPattern =
                    "^https?:\\/\\/(localhost|((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4})\\:\\d{1,5}\\/$";
            if (!url.matches(urlPattern))
                throw new IllegalArgumentException(languageMap.get("notFormatURL").toString());

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
    public void testMail(){
        Paint color = emailStatus.getTextFill();
        emailStatus.setText((String) statusAniMap.get("wait"));
        emailStatus.setOpacity(1.0);

        new Thread(() -> {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(4), emailStatus);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(e -> emailStatus.setTextFill(color));
            if(!emailService.sendDelfaultEmail()){
                // TODO: show No email was sent
                Platform.runLater(() ->{
                    emailStatus.setTextFill(javafx.scene.paint.Color.RED);
                    emailStatus.setText((String) statusAniMap.get("fail"));
                    fadeTransition.play();
                });
            } else {
                // Todo: show Email was sent
                Platform.runLater(() ->{
                    emailStatus.setText((String) statusAniMap.get("success"));
                    emailStatus.setTextFill(Color.LIGHTGREEN);
                    fadeTransition.play();
                });
            }
        }).start();
    }
    public void goBack() {
        mainCtrl.showOverview();
    }

}
