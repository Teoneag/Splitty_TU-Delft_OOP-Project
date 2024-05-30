package client.scenes;

import client.services.ConfigService;
import client.services.CurrencyService;
import client.services.I18NService;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PageWithMenuCtrl implements Initializable {

    private final MainCtrl mainCtrl;
    private final I18NService i18NService;
    private final CurrencyService currencyService;
    private final ConfigService configService;

    @FXML
    private StackPane stackPane;
    @FXML
    private Menu currencyMenu;
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu helpMenu;
    @FXML
    private Menu languageMenu;
    @FXML
    private MenuItem settings;
    @FXML
    private MenuItem shortcuts;
    @FXML
    private Button backButton;

    @Inject
    public PageWithMenuCtrl(MainCtrl mainCtrl, I18NService i18NService, CurrencyService currencyService, ConfigService configService) {
        this.mainCtrl = mainCtrl;
        this.i18NService = i18NService;
        this.currencyService = currencyService;
        this.configService = configService;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setLanguage();
        setCurrencyMenu();
        setLanguageMenu();
    }

    public void setCenter(Node node) {
        setCenter(node, false);
    }

    public void setCenter(Node node, boolean isHome) {
        stackPane.getChildren().clear();
        stackPane.getChildren().add(node);
        backButton.setVisible(!isHome);
    }

    private void setLanguage() {
        i18NService.setTranslation(fileMenu, "file");
        i18NService.setTranslation(settings, "settings");
        i18NService.setTranslation(helpMenu, "help");
        i18NService.setTranslation(shortcuts, "shortcuts");
        i18NService.setTranslation(backButton, "go_back");
    }

    private void setCurrencyMenu() {
        currencyMenu.setText(configService.getConfigCurrency());
        ToggleGroup currencyGroup = new ToggleGroup();
        currencyService.getCurrencies().forEach(currency -> {
            RadioMenuItem item = new RadioMenuItem(currency);
            item.setToggleGroup(currencyGroup);
            item.setOnAction(e -> {
                mainCtrl.setCurrency(currency);
                currencyMenu.setText(currency);
            });
            currencyMenu.getItems().add(item);
        });
    }

    private void setLanguageMenu() {
        final String initialLanguage = configService.getConfigLanguage();
        languageMenu.setText(initialLanguage);
        languageMenu.setGraphic(new ImageView(i18NService.getFlag(initialLanguage)));
        ToggleGroup languageGroup = new ToggleGroup();
        List<String> languages = i18NService.getLanguages();
        for (int i = 0; i < languages.size(); i++) {
            String language = languages.get(i);
            final int index = i;
            RadioMenuItem item = new RadioMenuItem(language, new ImageView(i18NService.getFlag(language)));
            item.setToggleGroup(languageGroup);
            item.setOnAction(e -> {
                configService.setConfigLanguage(language);
                i18NService.setLanguage(index);
                ((ImageView) languageMenu.getGraphic()).setImage(i18NService.getFlag(language));
                languageMenu.setText(language);
            });
            languageMenu.getItems().add(item);
        }
    }

    @FXML
    private void showSettings() {
        mainCtrl.showSettings();
    }

    @FXML
    private void showShortcuts() {
        mainCtrl.showShortcuts();
    }

    @FXML
    public void backToHome() {
        mainCtrl.showHome();
    }
}
