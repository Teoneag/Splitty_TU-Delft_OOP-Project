/**
 * Controller class for adding or editing tags.
 */
package client.scenes;

import client.services.ErrorService;
import client.services.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Tag;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AddTagCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ErrorService errorService;
    private Event event;
    private Expense expense;
    private Tag tag;
    private boolean fromManage;

    @FXML
    private TextField tagName;
    @FXML
    private ColorPicker tagColor;

    @FXML
    private Button addTagConfirm;
    @FXML
    private Button backButton;

    /**
     * Constructor for the AddTagCtrl.
     *
     * @param server       The server utilities.
     * @param mainCtrl     The main controller.
     * @param event        The event to which the tag belongs.
     * @param expense      The expense associated with the tag (null if not associated with any expense).
     * @param tag          The tag to be added or edited.
     * @param errorService The error service.
     */
    @Inject
    public AddTagCtrl(ServerUtils server, MainCtrl mainCtrl, Event event, Expense expense, Tag tag,
                      ErrorService errorService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.expense = expense;
        this.tag = tag;
        this.errorService = errorService;
    }

    /**
     * Initializes the controller.
     *
     * @param location  The location of the FXML file.
     * @param resources The resources used by the controller.
     */
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Refreshes the event and resets the tag.
     *
     * @param event The event to refresh.
     */
    public void refresh(Event event) {
        this.event = event;
        this.expense = null;
        tag = new Tag();
        fromManage = false;
    }

    /**
     * Refreshes the event and expense, and resets the tag.
     *
     * @param event   The event to refresh.
     * @param expense The expense associated with the tag.
     */
    public void refreshEdit(Event event, Expense expense) {
        this.event = event;
        this.expense = expense;
        tag = new Tag();
        fromManage = false;
    }

    /**
     * Adds or updates the tag to the event.
     */
    public void addTag() {


        try {
            String newName = tagName.getText();
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
        this.expense = null;
        this.tag = tag;
        tagName.setText(tag.getName());
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
        tagName.clear();
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


    /**
     * Set the language of the page
     *
     * @param map the language map which contains the translation
     * @throws IOException if the language file is not found
     */
    public void setLanguage(HashMap<String, Object> map) {
        tagName.setText((String) map.get("tagName"));
        addTagConfirm.setText((String) map.get("addTagConfirm"));
        backButton.setText((String) map.get("backButton"));

        // Set button sizes based on text length
        mainCtrl.setDynamicButtonSize(addTagConfirm);
        mainCtrl.setDynamicButtonSize(backButton);
    }
}
