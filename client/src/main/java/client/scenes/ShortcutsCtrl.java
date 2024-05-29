package client.scenes;

import client.services.I18NService;
import com.google.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ShortcutsCtrl implements Initializable {
    private final I18NService i18nService;

    @FXML
    private Label titleLabel;
    @FXML
    private TableView<Shortcut> shortcutsTable;
    @FXML
    private TableColumn<Shortcut, String> actionColumn, pageColumn, shortcut1Column, shortcut2Column, shortcut3Column;

    /**
     * Constructor for the ShortcutsCtrl.
     *
     * @param i18nService the language service
     */
    @Inject
    public ShortcutsCtrl(I18NService i18nService) {
        this.i18nService = i18nService;
    }

    /**
     * Initialize the shortcuts page
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        i18nService.setTranslation(titleLabel, "shortcuts.title");
        i18nService.setTranslation(actionColumn, "table.action");
        i18nService.setTranslation(pageColumn, "table.page");
        i18nService.setTranslation(shortcut1Column, "table.shortcut1");
        i18nService.setTranslation(shortcut2Column, "table.shortcut2");
        i18nService.setTranslation(shortcut3Column, "table.shortcut3");

        // Setting the cell value factory to map the column to the appropriate property
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        pageColumn.setCellValueFactory(new PropertyValueFactory<>("page"));
        shortcut1Column.setCellValueFactory(new PropertyValueFactory<>("shortcut1"));
        shortcut2Column.setCellValueFactory(new PropertyValueFactory<>("shortcut2"));
        shortcut3Column.setCellValueFactory(new PropertyValueFactory<>("shortcut3"));

        updateTableItems();
        i18nService.localeProperty().addListener((observable, oldValue, newValue) -> {
            updateTableItems();
        });
    }

    private void updateTableItems() {
        shortcutsTable.getItems().clear();  // Clear existing items to avoid duplicates
        addShortcut("show.help", "any.page", "shortcut.ctrl.shift.slash", "shortcut.ctrl.slash", "");
        addShortcut("go.back", "any.page", "shortcut.ctrl.z", "shortcut.alt.left.arrow", "shortcut.mouse.button4");
        addShortcut("navigate.between", "any.page", "shortcut.tab.forward", "shortcut.shift.tab.backward", "");
        addShortcut("navigate.table", "any.page", "shortcut.up.arrow", "shortcut.down.arrow", "");
        addShortcut("perform.action", "any.page", "shortcut.space", "shortcut.enter", "");
        addShortcut("exit.input.field", "any.page", "shortcut.esc", "", "");
        addShortcut("create.participant", "event.page", "shortcut.ctrl.p", "", "");
        addShortcut("create.expense", "event.page", "shortcut.ctrl.e", "", "");
    }

    private void addShortcut(String actionKey, String pageKey, String shortcut1Key, String shortcut2Key, String shortcut3Key) {
        shortcutsTable.getItems().add(new Shortcut(i18nService.get(actionKey), i18nService.get(pageKey), i18nService.get(shortcut1Key), i18nService.get(shortcut2Key), i18nService.get(shortcut3Key)));
    }

    public static class Shortcut {
        private final StringProperty action;
        private final StringProperty page;
        private final StringProperty shortcut1;
        private final StringProperty shortcut2;
        private final StringProperty shortcut3;

        public Shortcut(String action, String page, String shortcut1, String shortcut2, String shortcut3) {
            this.action = new SimpleStringProperty(action);
            this.page = new SimpleStringProperty(page);
            this.shortcut1 = new SimpleStringProperty(shortcut1);
            this.shortcut2 = new SimpleStringProperty(shortcut2);
            this.shortcut3 = new SimpleStringProperty(shortcut3);
        }

        public String getAction() {
            return action.get();
        }

        public StringProperty actionProperty() {
            return action;
        }

        public String getPage() {
            return page.get();
        }

        public StringProperty pageProperty() {
            return page;
        }

        public String getShortcut1() {
            return shortcut1.get();
        }

        public StringProperty shortcut1Property() {
            return shortcut1;
        }

        public String getShortcut2() {
            return shortcut2.get();
        }

        public StringProperty shortcut2Property() {
            return shortcut2;
        }

        public String getShortcut3() {
            return shortcut3.get();
        }

        public StringProperty shortcut3Property() {
            return shortcut3;
        }
    }
}
