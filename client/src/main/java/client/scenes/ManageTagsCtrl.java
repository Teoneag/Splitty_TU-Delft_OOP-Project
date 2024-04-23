/**
 * Controller class for managing tags associated with an event.
 */
package client.scenes;

import client.services.DebtUtils;
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
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;

import java.awt.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ManageTagsCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final DebtUtils debt;
    private Event event;
    private Map<PieChart.Data, Integer> dataColors;

    @FXML
    private Text eventTotal;
    @FXML
    private TableView<Tag> tagsTable;
    @FXML
    private TableColumn<Tag, String> tagName;
    @FXML
    private TableColumn<Tag, String> tagAmount;
    @FXML
    private PieChart pieChart;

    @FXML
    private Button backButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Text eventTotalText;



    /**
     * Constructor for the ManageTagsCtrl.
     *
     * @param server   The server utility.
     * @param mainCtrl The main controller.
     * @param debt     The debt utility.
     * @param event    The event.
     */
    @Inject
    public ManageTagsCtrl(ServerUtils server, MainCtrl mainCtrl, DebtUtils debt, Event event) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.debt = debt;
        this.event = event;
    }

    /**
     * Initializes the controller.
     *
     * @param location  The location of the FXML file.
     * @param resources The resources used by the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tagName.setCellValueFactory(t -> new ReadOnlyObjectWrapper<>(t.getValue().getName()));
        tagAmount.setCellValueFactory(t -> new ReadOnlyObjectWrapper<>(
                debt.formattedAmount(getTagAmount(t.getValue()))
        ));
        tagName.setCellFactory(tc -> new TableCell<>() {
            {
                itemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        setText(null);
                        setStyle(null);
                    } else {
                        setText(getItem());
                        Color c = new Color(getTableRow().getItem().getColor());
                        setStyle(EventOverviewCtrl.cString(c));
                    }
                });
            }
        });
    }

    /**
     * Refreshes the controller with new event data.
     *
     * @param event The new event.
     */
    public void refresh(Event event) {
        this.event = event;
        eventTotal.setText(debt.formattedAmount(debt.expenseTotal(event)));
        populateTable();
        populatePieChart();
    }

    /**
     * Navigates back to the event overview screen.
     */
    public void back() {
        mainCtrl.showEventOverview(event.getInviteCode());
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

        if(getTagAmount(tag) != 0){
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
                server.deleteTagById(tag.getId());
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
            List<Tag> tags = server.getTagsByEvent(event.getInviteCode());
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

        List<Tag> allTags = server.getTagsByEvent(event.getInviteCode());
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Tag t : allTags) {
            PieChart.Data data = pieChartDataBuilder(t);
            dataColors.putIfAbsent(data, t.getColor());
            pieChartData.add(data);
        }

        pieChart.setData(pieChartData);
        pieChart.setTitle("Event By Tag");
        //TODO change this dynamically

        pieChart.getData().forEach(data -> {
            String percentage = String.format("%.2f%%", (data.getPieValue() / getTotal() * 100)) +
                    "\n" + debt.formattedAmount((float) data.getPieValue());
            Tooltip tooltip = new Tooltip(percentage);
            Tooltip.install(data.getNode(), tooltip);
            Color c = new Color(dataColors.get(data));
            String cString = String.format(
                    "rgba(%d,%d,%d,1)",
                    c.getRed(), c.getGreen(), c.getBlue()
            );
            data.getNode().setStyle("-fx-pie-color: " + cString);
        });
    }

    /**
     * Builds the data for the pie chart.
     *
     * @param tag The tag to build the data for.
     * @return The data for the pie chart.
     */
    public PieChart.Data pieChartDataBuilder(Tag tag){
        return new PieChart.Data(tag.getName(), getTagAmount(tag));
    }

    /**
     * Takes the Text amount of the event and returns it as a float.
     *
     * @return The total amount of the event.
     */
    private float getTotal() {
        String[] totalAmount = eventTotal.getText().split(" ");
        String totalWithDot = totalAmount[1].replace(",", ".");
        return Float.parseFloat(totalWithDot);
    }

    /**
     * Gets the total amount of expenses associated with a tag.
     *
     * @param tag The tag to get the amount for.
     * @return The total amount of expenses associated with the tag.
     */
    public float getTagAmount(Tag tag) {
        List<Expense> expenseList = server.getTransactionsByCurrency(event.getInviteCode());
        return (float) expenseList.stream()
                .filter(e -> e.getTag().equals(tag))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    /**
     * Set the language of the page
     *
     * @param map the language map which contains the translation
     */
    public void setLanguage(HashMap<String, Object> map) {
        tagName.setText((String) map.get("tagNameColumn"));
        tagAmount.setText((String) map.get("tagAmountColumn"));
        backButton.setText((String) map.get("backButton"));
        deleteButton.setText((String) map.get("deleteButton"));
        eventTotalText.setText((String) map.get("eventTotalText"));

        // Set button sizes based on text length
        mainCtrl.setDynamicButtonSize(backButton);
        mainCtrl.setDynamicButtonSize(deleteButton);
    }
}
