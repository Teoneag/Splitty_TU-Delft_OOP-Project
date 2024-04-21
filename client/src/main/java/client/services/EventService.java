package client.services;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

@Service
public class EventService {

    private final String recentEventsFilePath = "src/main/java/client/config/recent_events.txt";
    private final Pattern validTextPattern = Pattern.compile("^[a-zA-Z0-9 .,!?&@#\\-]{1,100}$");
    private final int minTitleLength = 3;
    private final int maxTitleLength = 25;
    private final int minDescriptionLength = 3;
    private final int maxDescriptionLength = 69;
    private String tooShort = "Too short!";
    private String tooLong = "Too long!";
    private String invalidCharacters = "Invalid characters used!";

    /**
     * Gets the recent events from the recent events file
     * @return a list of recent events
     */
    public List<String> getRecentEvents() {
        try {
            return Files.readAllLines(Paths.get(recentEventsFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds an event to the recent events file
     *
     * @param inviteCode the invite code of the event
     */
    public void addToRecentEvents(String inviteCode) {
        try {
            Path path = Paths.get(recentEventsFilePath);

            List<String> lines = Files.readAllLines(path);
            if (!lines.contains(inviteCode)) {
                lines.add(inviteCode);
                Files.write(path, lines, StandardOpenOption.WRITE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clears the recent events file
     */
    public void clearRecentEvents() {
        try {
            Path path = Paths.get(recentEventsFilePath);
            Files.write(path, "".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Validates the title of an event
     *
     * @param field the text field to validate
     * @param error the label to display the error message
     */
    public void titleCheck(TextField field, Label error) {
        applyValidation(field, error, minTitleLength, maxTitleLength);
    }

    /**
     * Validates the description of an event
     *
     * @param field the text field to validate
     * @param error the label to display the error message
     */
    public void descriptionCheck(TextField field, Label error) {
        applyValidation(field, error, minDescriptionLength, maxDescriptionLength);
    }

    /**
     * Sets the language of the validation messages
     *
     * @param map the map containing the language
     */
    public void setLanguage(HashMap<String, Object> map) {
        tooShort = (String) map.get("tooShort");
        tooLong = (String) map.get("tooLong");
        invalidCharacters = (String) map.get("invalidChar");
    }

    /**
     * Applies validation to a text field
     *
     * @param input     the text field to apply validation to
     * @param error     the label to display the error message
     * @param minLength the minimum acceptable length of the text
     * @param maxLength the maximum acceptable length of the text
     */
    public void applyValidation(TextField input, Label error, int minLength, int maxLength) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            String validation = validateText(newText, minLength, maxLength);
            error.setText(validation);
            if (validation.isEmpty() || validation.equals(tooShort)) return change;
            return null;
        };
        input.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Validates the title and the description
     *
     * @param title      title
     * @param titleError titleError
     * @param desc       description
     * @param descError  description Error
     * @return true if good
     */
    public boolean validateTitleAndDesc(TextField title, Label titleError, TextField desc, Label descError) {
        final String resRes = validateText(title.getText(), minTitleLength, maxTitleLength);
        titleError.setText(resRes);
        final String resDesc = validateText(desc.getText(), minDescriptionLength, maxDescriptionLength);
        descError.setText(resDesc);
        return resRes.isEmpty() && resDesc.isEmpty();
    }

    /**
     * Validates text
     *
     * @param text      the text to validate
     * @param minLength the minimum acceptable length of the text
     * @param maxLength the maximum acceptable length of the text
     * @return a validation response indicating success or the type of error
     */
    public String validateText(String text, int minLength, int maxLength) {
        if (text.length() > maxLength) return tooLong;
        if (text.length() < minLength) return tooShort;
        if (!validTextPattern.matcher(text).matches()) return invalidCharacters;
        return "";
    }
}
