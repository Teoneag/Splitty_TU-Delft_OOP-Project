package client.services;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

public class StyleUtils {
    /**
     * Sets the background insets for various elements of the table
     * based on whether the vertical scrollbar is active or not
     * @param table the table to compute insets for
     * @param <T> the class which the table holds
     */
    public <T> void computeTableInsets(TableView<T> table) {
        Platform.runLater(() -> {
            StackPane header = (StackPane) table.lookup(".column-header-background");
            ScrollBar hBar = getHBar(table);
            while (header == null || hBar == null) {
                try {
                    Thread.sleep(200);
                    header = (StackPane) table.lookup(".column-header-background");
                    hBar = getHBar(table);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (table.lookup(".scroll-bar:vertical").isVisible()) {
                header.setStyle("-fx-background-insets: 0 12px 0 0");
                hBar.setStyle("-fx-background-insets: 0 0 0 0");
            } else {
                header.setStyle("-fx-background-insets:  0 2px 0 0");
                hBar.setStyle("-fx-background-insets: 0 2px 0 0");
            }
        });
    }

    /**
     * get the horizontal scrollbar of the table
     * @param table the table to get the scrollbar from
     * @return the horizontal scrollbar of the table, or null if none exists
     * @param <T> what the tableview contains
     */
    public <T> ScrollBar getHBar(TableView<T> table) {
        ScrollBar result;
        result = (ScrollBar) table.lookupAll(".scroll-bar").stream().filter(n -> {
            if (n instanceof ScrollBar bar)
                return bar.getOrientation().equals(Orientation.HORIZONTAL);
            return false;
        }).findAny().orElse(null);
        return result;
    }
}