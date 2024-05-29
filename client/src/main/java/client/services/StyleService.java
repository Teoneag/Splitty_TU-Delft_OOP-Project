package client.services;

import com.google.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class StyleService {

    private final I18NService i18NService;
    private final Map<Label, FadeTransition> fadeTransitions;
    private final List<String> themes = List.of("dark", "light");

    @Inject
    public StyleService(I18NService i18NService) {
        this.i18NService = i18NService;
        this.fadeTransitions = new HashMap<>();
    }

    public List<String> getThemes() {
        return themes;
    }

    public String getTheme(int index) {
        return themes.get(index);
    }

    public void copyInviteCode(Label label, String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
        playFadeTransition(label);
        playSoundEffect();
    }

    /**
     * Plays a sound effect when the invite code is copied.
     */
    private void playSoundEffect() {
        try {
            final URI uri = new ClassPathResource("Heavenly Sound Effect Meme (Perfectly Cut).mp3").getURI();
            MediaPlayer mediaPlayer = new MediaPlayer(new Media(uri.toString()));
            mediaPlayer.play();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void playFadeTransition(Label label) {
        playFadeTransition(label, () -> label.setVisible(false), () -> label.setVisible(true));
    }

    public void playFadeTransition(Label label, String key) {
        playFadeTransition(label, () -> i18NService.setText(label, ""), () -> i18NService.setTranslation(label, key));
    }

    private void playFadeTransition(Label label, Runnable onFinished, Runnable prePlay) {
        FadeTransition fadeTransition = fadeTransitions.get(label);
        if (fadeTransition != null) {
            fadeTransition.stop();
        } else {
            fadeTransition = new FadeTransition(Duration.seconds(5), label);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(event -> onFinished.run());
            fadeTransitions.put(label, fadeTransition);
        }
        prePlay.run();
        fadeTransition.play();
    }


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