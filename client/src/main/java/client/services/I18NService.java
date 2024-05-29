// Inspired by: https://www.sothawo.com/2016/09/how-to-implement-a-javafx-ui-where-the-language-can-be-changed-dynamically/

package client.services;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

// I18N = Internationalization = Language Service
final public class I18NService {

    /**
     * the current selected Locale.
     */
    private final ObjectProperty<Locale> locale;

    public I18NService() {
        locale = new SimpleObjectProperty<>(getDefaultLocale());
        locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
    }

    public Image getFlag(String language) {
        return new Image("images/" + language + ".png", 35, 20, false, true);
    }

    public List<String> getLanguages() {
        return getSupportedLocales().stream().map(Locale::getDisplayLanguage).toList();
    }

    public void setLanguage(int index) {
        setLocale(getSupportedLocales().get(index));
    }

    public void setLanguage(String language) {
        setLocale(getSupportedLocales().stream().filter(
            locale -> locale.getDisplayLanguage().equals(language)).findFirst().orElse(Locale.ENGLISH));
    }

    /**
     * get the supported Locales.
     *
     * @return List of Locale objects.
     */
    public List<Locale> getSupportedLocales() {
        return List.of(Locale.ENGLISH, Locale.of("ro", "RO"), Locale.of("nl", "NL"));
    }

    /**
     * @return default locale
     */
    public Locale getDefaultLocale() {
        Locale sysDefault = Locale.getDefault();
        return getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
    }

    public Locale getLocale() {
        return locale.get();
    }

    public void setLocale(Locale locale) {
        localeProperty().set(locale);
        Locale.setDefault(locale);
    }

    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    /**
     * gets the string with the given key from the resource bundle for the current locale and uses it as first argument
     * to MessageFormat.format, passing in the optional args and returning the result.
     *
     * @param key  message key
     * @param args optional arguments for the message
     * @return localized formatted string
     */
    public String get(final String key, final Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("Messages", getLocale());
        if (key.isEmpty()) return "";
        return MessageFormat.format(bundle.getString(key), args);
    }

    /**
     * creates a String binding to a localized String for the given message bundle key
     *
     * @param key key
     * @return String binding
     */
    public StringBinding createStringBinding(final String key, Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), locale);
    }

    /**
     * creates a String Binding to a localized String that is computed by calling the given func
     *
     * @param func function called on every change
     * @return StringBinding
     */
    public StringBinding createStringBinding(Callable<String> func) {
        return Bindings.createStringBinding(func, locale);
    }

    public void setTranslation(Label control, String key) {
        if (key.isEmpty()) setText(control, key);
        else control.textProperty().bind(createStringBinding(key));
    }

    public void setTranslation(Stage control, String key) {
        control.titleProperty().bind(createStringBinding(key));
    }

    @SuppressWarnings("rawtypes")
    public void setTranslation(TableView table, String key) {
        table.setPlaceholder(new Label(get(key)));
    }

    public void setTranslation(CheckBox control, String key) {
        control.textProperty().bind(createStringBinding(key));
    }

    public void setTranslation(Label control, String key, Object... args) {
        if (key.isEmpty()) setText(control, key);
        else control.textProperty().bind(createStringBinding(key, args));
    }

    public void setTranslation(Text control, String key) {
        control.textProperty().bind(createStringBinding(key));
    }

    public void setTranslation(Tab control, String key) {
        control.textProperty().bind(createStringBinding(key));
    }

    public void setTranslation(Button control, String key) {
        control.textProperty().bind(createStringBinding(key));
    }

    public void setTranslation(TextField control, String key) {
        control.promptTextProperty().bind(createStringBinding(key));
    }

    public void setTranslation(ComboBox control, String key) {
        control.promptTextProperty().bind(createStringBinding(key));
    }

    public void setTranslation(TableColumn control, String key) {
        control.textProperty().bind(createStringBinding(key));
    }

    public void setTranslation(Menu control, String key) {
        control.textProperty().bind(createStringBinding(key));
    }

    public void setTranslation(MenuItem control, String key) {
        control.textProperty().bind(createStringBinding(key));
    }

    public void setTranslation(Alert alert, String title, String header) {
        alert.titleProperty().bind(createStringBinding(title));
        alert.headerTextProperty().bind(createStringBinding(header));
    }

    public void setTranslation(Alert alert, String title, String header, String content) {
        setTranslation(alert, title, header);
        alert.contentTextProperty().bind(createStringBinding(content));
    }

    public void setTranslation(ChoiceBox<String> choiceBox, List<String> keys) {
        choiceBox.itemsProperty().bind(Bindings.createObjectBinding(() -> keys.stream().map(this::get).
                collect(Collectors.toCollection(FXCollections::observableArrayList)), localeProperty()));
    }

    public void setTranslation(ChoiceBox<String> choiceBox, String key) {
        choiceBox.setValue(get(key));
        localeProperty().addListener((observable, oldValue, newValue) -> choiceBox.setValue(get(key)));
    }

    public void setText(Label label, String text) {
        label.textProperty().unbind();
        label.setText(text);
    }

    public void setText(Stage stage, String text) {
        stage.titleProperty().unbind();
        stage.setTitle(text);
    }


    /**
     * creates a bound Label whose value is computed on language change.
     *
     * @param func the function to compute the value
     * @return Label
     */
    public Label labelFromKey(Callable<String> func) {
        Label label = new Label();
        label.textProperty().bind(createStringBinding(func));
        return label;
    }

    /**
     * creates a bound Label whose value is computed on language change.
     *
     * @param key ResourceBundle key
     * @return Label
     */
    public Label labelFromKey(String key) {
        Label label = new Label();
        label.setWrapText(true);
        label.setPadding(new Insets(20));
        setTranslation(label, key);
        return label;
    }

    /**
     * creates a bound Button whose value is computed on language change.
     *
     * @param key ResourceBundle key
     * @return MenuItem
     */
    public MenuItem menuItemFromKey(String key) {
        MenuItem menuItem = new MenuItem();
        menuItem.textProperty().bind(createStringBinding(key));
        return menuItem;
    }

    public Tooltip tooltipFromKey(String key) {
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(createStringBinding(key));
        return tooltip;
    }

    /**
     * creates a bound Button whose value is computed on language change.
     *
     * @param key ResourceBundle key
     * @return MenuItem
     */
    public Menu menuFromKey(String key) {
        Menu menu = new Menu();
        menu.textProperty().bind(createStringBinding(key));
        return menu;
    }

    /**
     * creates a bound Button for the given resourcebundle key
     *
     * @param key  ResourceBundle key
     * @param args optional arguments for the message
     * @return Button
     */
    public Button buttonForKey(final String key, final Object... args) {
        Button button = new Button();
        button.textProperty().bind(createStringBinding(key, args));
        return button;
    }

    /**
     * creates a bound Tooltip for the given resourcebundle key
     *
     * @param key  ResourceBundle key
     * @param args optional arguments for the message
     * @return Label
     */
    public Tooltip tooltipFromKey(final String key, final Object... args) {
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(createStringBinding(key, args));
        return tooltip;
    }

}