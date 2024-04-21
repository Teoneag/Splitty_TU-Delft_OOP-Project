package client.scenes;

import client.services.ConfigService;
import client.services.LanguageService;
import client.services.Shortcut;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.HashMap;


public class ShortcutsCtrl {
    private final MainCtrl mainCtrl;
    private final ConfigService configService;
    private final LanguageService languageService;
    private HashMap<String, Object> map;
    @FXML
    private TableView<Shortcut> shortcutsTable;
    @FXML
    private TableColumn<Shortcut, String> actionColumn, pageColumn, shortcut1Column,
        shortcut2Column, shortcut3Column;
    @FXML
    private Button backButton;

    /**
     * Constructor for the ShortcutsCtrl.
     * @param mainCtrl the main controller
     * @param configService the config service
     * @param languageService the language service
     */
    @Inject
    public ShortcutsCtrl(MainCtrl mainCtrl,
                         ConfigService configService, LanguageService languageService) {
        this.mainCtrl = mainCtrl;
        this.configService = configService;
        this.languageService = languageService;
    }

    /**
     * Initialize the shortcuts page
     */
    public void initialize() {
        // Set up the columns to use the Shortcut properties
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        pageColumn.setCellValueFactory(new PropertyValueFactory<>("pageForAction"));
        shortcut1Column.setCellValueFactory(new PropertyValueFactory<>("shortcut1"));
        shortcut2Column.setCellValueFactory(new PropertyValueFactory<>("shortcut2"));
        shortcut3Column.setCellValueFactory(new PropertyValueFactory<>("shortcut3"));
        this.map = configService.getLanguage();
        // Create and add the shortcut data
        shortcutsTable.setItems(languageService.setTran(map));
    }


    /**
     * Set the language of the page
     * @param map2 the language map
     */
    public void setLanguage(HashMap<String, Object> map2) {
        backButton.setText((String) map2.get("back"));
        actionColumn.setText((String) map2.get("action"));
        pageColumn.setText((String) map2.get("page"));
        shortcutsTable.setItems(languageService.setTran(map2));

        this.map = map2;
        mainCtrl.setDynamicButtonSize(backButton);

    }

    @FXML
    public void goBack() {
        mainCtrl.showOverview();
    }
}
