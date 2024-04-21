package client.services;

import client.scenes.MainCtrl;
import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ListCell;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

@Service
public class LanguageService {
    private final List<String> languages = List.of("English", "Dutch", "Romanian");
    private final ConfigService configService;

    @Inject
    public LanguageService(ConfigService configService) {
        this.configService = configService;
    }

    private class ImageCell extends ListCell<String> {
        /**
         * Update the ImageView with the flag of the selected language
         *
         * @param item  The new item for the cell.
         * @param empty whether this cell represents data from the list. If it
         *              is empty, then it does not represent any domain data, but is a cell
         *              being used to render an "empty" row.
         */
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setGraphic(new ImageView(getFlag(item)));
            }
        }
    }

    /**
     * Set the languages in the language box
     *
     * @param languageBox the language box passed on from controller
     */
    public void setLanguagesComboBox(ComboBox<String> languageBox) {
        languageBox.getItems().addAll(languages);
        languageBox.setCellFactory(lv -> new ImageCell());
        languageBox.setButtonCell(new ImageCell());
        languageBox.getSelectionModel().select(configService.getConfigLanguage());
    }

    /**
     * Set the translations for the shortcut table
     *
     * @param map the language map
     * @return the shortcut list
     */
    public ObservableList<Shortcut> setTran(HashMap<String, Object> map) {
        return FXCollections.observableArrayList(
                new Shortcut(map.get("action1").toString(), map.get("pageForAction1").toString(),
                        "Ctrl + Shift + /", "Ctrl  + /", ""),
                new Shortcut(map.get("back").toString(), map.get("pageForAction1").toString(),
                        map.get("backButtonKb").toString(), "Alt + " + map.get("leftArrow").toString(),
                        map.get("backMouseButton").toString()),
                new Shortcut(map.get("action2").toString(), map.get("pageForAction1").toString(),
                        "Tab " + map.get("forward").toString(),
                        "Shift + Tab " + map.get("backWard").toString(), ""),
                new Shortcut(map.get("action3").toString(), map.get("pageForAction1").toString(),
                        map.get("upArrow").toString(), map.get("downArrow").toString(), ""),
                new Shortcut(map.get("action4").toString(), map.get("pageForAction1").toString(),
                        map.get("space").toString(), "Enter", ""),
                new Shortcut(map.get("action5").toString(), map.get("pageForAction1").toString(), "Esc",
                        "", ""),
                new Shortcut(map.get("action6").toString(), map.get("pageForAction2").toString(),
                        "Ctrl + P", "", ""),
                new Shortcut(map.get("expenseCreate").toString(), map.get("pageForAction2").toString(),
                        "Ctrl + E", "", ""));
    }

    /**
     * get the flag of the language
     *
     * @param language the language
     * @return the flag of the language
     */
    public Image getFlag(String language) {
        return new Image("images/" + language + ".png", 35, 20, false, true);
    }

    /**
     * change the language of the application without restarting it
     *
     * @param mainCtrl    the main controller
     * @param languageBox the language box
     */
    public void changeLanguage(MainCtrl mainCtrl, ComboBox<String> languageBox) {
        configService.setConfigLanguage(languageBox.getValue());
        mainCtrl.switchLanguage();
    }

    /**
     * download the language template
     */
    public void downloadLangTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save language template");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        fileChooser.setInitialFileName(
                "language_template_" + configService.getConfigLanguage() + ".json");
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        Path sourcePath = Path.of(
                "src/main/java/client/languages/" + configService.getConfigLanguage() + ".json");
        Path destinationPath = file.toPath();
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
