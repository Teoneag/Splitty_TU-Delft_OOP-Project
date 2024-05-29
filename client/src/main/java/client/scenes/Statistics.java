/**
 * Controller class for managing tags associated with an event.
 */
package client.scenes;

import client.services.DebtService;
import client.services.I18NService;
import client.services.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Tag;
import jakarta.ws.rs.ProcessingException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.awt.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class Statistics implements Initializable {

    private final ServerUtils serverUtils;
    private final MainCtrl mainCtrl;
    private final I18NService i18NService;
    private final DebtService debtService;

    private Event event;
    private Map<PieChart.Data, Integer> dataColors;

    @FXML
    private Label statisticsAndTagsLabel;
    @FXML
    private TableView<Tag> tagsTable;
    @FXML
    private Button deleteButton;
    @FXML
    private PieChart pieChart;
    @FXML
    private TableColumn<Tag, String> tagName;
    @FXML
    private TableColumn<Tag, String> tagAmount;
    @FXML
    private Label eventTotal;

    /**
     * Constructor for the ManageTagsCtrl.
     *
     * @param serverUtils The server utility.
     * @param mainCtrl    The main controller.
     * @param debtService The debt utility.
     * @param event       The event.
     */
    @Inject
    public Statistics(ServerUtils serverUtils, MainCtrl mainCtrl, DebtService debtService, Event event, I18NService i18NService) {
        this.serverUtils = serverUtils;
        this.mainCtrl = mainCtrl;
        this.debtService = debtService;
        this.event = event;
        this.i18NService = i18NService;
    }

    /**
     * Initializes the controller.
     *
     * @param location  The location of the FXML file.
     * @param resources The resources used by the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setLanguage();

        tagName.setCellValueFactory(t -> new ReadOnlyObjectWrapper<>(t.getValue().getName()));
        tagAmount.setCellValueFactory(t -> new ReadOnlyObjectWrapper<>(debtService.formattedAmount(getTagAmount(t.getValue()))));
        tagName.setCellFactory(tc -> new TableCell<>() {
            {
                itemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        setText(null);
                        setStyle(null);
                    } else {
                        setText(getItem());
                        Color c = new Color(getTableRow().getItem().getColor());
                        setStyle(EventCtrl.cString(c));
                    }
                });
            }
        });
    }

    /**
     * Set the language of the page
     */
    public void setLanguage() {
        i18NService.setTranslation(statisticsAndTagsLabel, "statistics");
        i18NService.setTranslation(tagName, "tag.name");
        i18NService.setTranslation(tagAmount, "tag.amount");
        i18NService.setTranslation(tagsTable, "table.noContent");
        i18NService.setTranslation(deleteButton, "delete");
        i18NService.localeProperty().addListener((observable, oldValue, newValue) -> pieChart.setTitle(i18NService.get("expenses.by.tag")));
        i18NService.setTranslation(eventTotal, "event.total",
            debtService.formattedAmount(debtService.expenseTotal(event)));
    }

    /**
     * Refreshes the controller with new event data.
     *
     * @param event The new event.
     */
    public void refresh(Event event) {
        this.event = event;
//        eventTotal.setText(debtService.formattedAmount(debtService.expenseTotal(event))); ToDO
        populateTable();
        populatePieChart();
    }

    /**
     * Navigates back to the event overview screen.
     */
    public void back() {
        mainCtrl.showEvent(event.getInviteCode());
    }

    /**
     * Deletes the selected tag.
     */
    public void deleteTag() {
        if (tagsTable.getSelectionModel().getSelectedItem() == null) {
            // TODO: Show error message
            return;
        }
        int i = tagsTable.getSelectionModel().getFocusedIndex();
        Tag tag = tagsTable.getItems().get(i);

        if (getTagAmount(tag) != 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot delete tag");
            alert.setContentText("There are expenses associated with this tag. Please remove them first.");
            alert.showAndWait();
            return;
        }
        //TODO check if there are expenses with this tag, if so show a warning

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Tag");
        alert.setHeaderText("Are you sure you want to delete this tag?");
        alert.setContentText("You are about to delete tag: " + tag.getName() + "\nThis action cannot be undone.");
        alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
            try {
                serverUtils.deleteTagById(tag.getId());
                populateTable();
            } catch (ProcessingException e) {
                if (e.getCause().getClass() == ConnectException.class) {
                    mainCtrl.serverConnectionAlert();
                    return;
                }
                throw e;
            }
        });
    }

    /**
     * Handles events when tags are clicked.
     */
    public void tagsOnClick() {
        tagsTable.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                Tag selectedItem = tagsTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    mainCtrl.showEditTag(this.event, selectedItem);
                }
                e.consume(); // Consume the event to prevent further propagation
            }
        });

        tagsTable.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Tag selectedItem = tagsTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    mainCtrl.showEditTag(this.event, selectedItem);
                }
                e.consume(); // Consume the event to prevent further propagation
            }
        });
    }

    /**
     * Populates the table with tags associated with the event.
     */
    public void populateTable() {
        try {
            List<Tag> tags = serverUtils.getTagsByEvent(event.getInviteCode());
            tagsTable.getItems().clear();
            tagsTable.getItems().addAll(tags);
            tagsTable.refresh();
        } catch (ProcessingException e) {
            if (e.getCause().getClass() == ConnectException.class) {
                mainCtrl.serverConnectionAlert();
                return;
            }
            throw e;
        }
    }

    /**
     * Populates the pie chart with expenses associated with tags.
     */
    public void populatePieChart() {
        dataColors = new HashMap<>();

        List<Tag> allTags = serverUtils.getTagsByEvent(event.getInviteCode());
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Tag t : allTags) {
            PieChart.Data data = pieChartDataBuilder(t);
            dataColors.putIfAbsent(data, t.getColor());
            pieChartData.add(data);
        }

        pieChart.setData(pieChartData);

        pieChart.getData().forEach(data -> {
            String percentage = String.format("%.2f%%", (data.getPieValue() / getTotal() * 100)) + "\n" + debtService.formattedAmount((float) data.getPieValue());
            Tooltip tooltip = new Tooltip(percentage);
            Tooltip.install(data.getNode(), tooltip);
            Color c = new Color(dataColors.get(data));
            String cString = String.format("rgba(%d,%d,%d,1)", c.getRed(), c.getGreen(), c.getBlue());
            data.getNode().setStyle("-fx-pie-color: " + cString);
        });
    }

    /**
     * Builds the data for the pie chart.
     *
     * @param tag The tag to build the data for.
     * @return The data for the pie chart.
     */
    public PieChart.Data pieChartDataBuilder(Tag tag) {
        return new PieChart.Data(tag.getName(), getTagAmount(tag));
    }

    /**
     * Takes the Text amount of the event and returns it as a float.
     *
     * @return The total amount of the event.
     */
    private float getTotal() {
        // ToDo
//        String[] totalAmount = eventTotal.getText().split(" ");
//        String totalWithDot = totalAmount[1].replace(",", ".");
//        return Float.parseFloat(totalWithDot);
        return 0.0F;
    }

    /**
     * Gets the total amount of expenses associated with a tag.
     *
     * @param tag The tag to get the amount for.
     * @return The total amount of expenses associated with the tag.
     */
    public float getTagAmount(Tag tag) {
        List<Expense> expenseList = serverUtils.getTransactionsByCurrency(event.getInviteCode());
        return (float) expenseList.stream().filter(e -> e.getTag().equals(tag)).mapToDouble(Expense::getAmount).sum();
    }
}
