/**
 * Controller class for adding or editing tags.
 */
package client.scenes;

import client.services.ErrorService;
import client.services.I18NService;
import client.services.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Tag;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;

import java.net.URL;
import java.util.ResourceBundle;

public class AddTagCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ErrorService errorService;
    private final I18NService i18NService;

    private Event event;
    private Tag tag;
    private boolean fromManage;

    @FXML
    private Label addTagLabel;
    @FXML
    private TextField tagNameField;
    @FXML
    private ColorPicker tagColor;
    @FXML
    private Button confirmButton;

    /**
     * Constructor for the AddTagCtrl.
     *
     * @param server       The server utilities.
     * @param mainCtrl     The main controller.
     * @param event        The event to which the tag belongs.
     * @param tag          The tag to be added or edited.
     * @param errorService The error service.
     */
    @Inject
    public AddTagCtrl(ServerUtils server, MainCtrl mainCtrl, Event event, Tag tag,
                      ErrorService errorService, I18NService i18NService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.tag = tag;
        this.errorService = errorService;
        this.i18NService = i18NService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setLanguage();
    }

    /**
     * Set the language of the page
     */
    public void setLanguage() {
        i18NService.setTranslation(addTagLabel, "add.tag");
        i18NService.setTranslation(tagNameField, "tag.name");
        i18NService.setTranslation(confirmButton, "confirm");
    }

    /**
     * Refreshes the event and resets the tag.
     *
     * @param event The event to refresh.
     */
    public void refresh(Event event) {
        this.event = event;
        tag = new Tag();
        fromManage = false;
    }

    /**
     * Refreshes the event and expense, and resets the tag.
     *
     * @param event The event to refresh.
     */
    public void refreshEdit(Event event) {
        this.event = event;
        tag = new Tag();
        fromManage = false;
    }

    /**
     * Adds or updates the tag to the event.
     */
    public void addTag() {


        try {
            String newName = tagNameField.getText();
            if (newName.isEmpty())
                throw new IllegalArgumentException("Tag cannot have empty name");
            if (newName.equals("Payment"))
                throw new IllegalArgumentException(
                    "That tag name is reserved, please choose another");
            tag.setName(newName);

            tag.setColor(anotherWeirdColorChanger());

            tag.setEvent(event);

            if (tag.getId() != 0) {
                server.updateTag(tag);
                clearFields();
                mainCtrl.showManageTags(tag.getEvent());
                return;
            }
            clearFields();
            mainCtrl.showAddExpenseReduced(server.addTag(tag));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Sets up the UI for editing a tag.
     *
     * @param event The event associated with the tag.
     * @param tag   The tag to be edited.
     */
    public void editTag(Event event, Tag tag) {
        this.event = event;
        this.tag = tag;
        tagNameField.setText(tag.getName());
        tagColor.setValue(javaFXColorToAwtColor(tag));
        fromManage = true;
    }

    /**
     * Navigates back to the add expense screen.
     */
    public void back() {
        try {
            if (fromManage)
                mainCtrl.showManageTags(event);
            else
                mainCtrl.showAddExpenseReduced(null);
        } catch (Exception e) {
            Alert alert = errorService.generalError(e.getMessage());
            alert.showAndWait();
        }

    }

    /**
     * Clears the input fields.
     */
    private void clearFields() {
        tagNameField.clear();
        tagColor.setValue(Color.WHITE);
    }

    /**
     * Converts a JavaFX Color to an AWT Color.
     *
     * @param tag The tag whose color is to be converted.
     * @return The AWT Color corresponding to the tag color.
     */
    private Color javaFXColorToAwtColor(Tag tag) {
        int color = tag.getColor();
        java.awt.Color awtColor = new java.awt.Color(color);
        double red = awtColor.getRed() / 255.0;
        double green = awtColor.getGreen() / 255.0;
        double blue = awtColor.getBlue() / 255.0;
        double alpha = awtColor.getAlpha() / 255.0;
        return new Color(red, green, blue, alpha);
    }

    /**
     * Converts a JavaFX Color to an AWT Color.
     *
     * @return The RGB integer value of the tag color.
     */
    private int anotherWeirdColorChanger() {
        double red = tagColor.getValue().getRed();
        double green = tagColor.getValue().getGreen();
        double blue = tagColor.getValue().getBlue();
        java.awt.Color tagColor =
            new java.awt.Color((int) (red * 255), (int) (green * 255), (int) (blue * 255));
        return tagColor.getRGB();
    }
}
