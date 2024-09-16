package client.services;

import com.google.inject.Inject;
import commons.Expense;
import commons.Participant;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Modality;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

final public class ErrorService {
    private final I18NService i18NService;
    private final StyleService styleService;

    private final Pattern validTextPattern = Pattern.compile("^[a-zA-Z0-9 .,!?&@#\\-]{0,100}$");
    private final int minTitleLength = 3;
    private final int maxTitleLength = 25;
    private final int minDescriptionLength = 3;
    private final int maxDescriptionLength = 69;
    private final int inviteCodeLength = 6;
    private final int passwordLength = 6;
    private final int minFirstNameLength = 1;
    private final int maxGenericLength = 50;
    private final String invalidCharacters = "invalid_characters";
    private final String tooShort = "too_short";
    private final String tooLong = "too_long";



    @Inject
    public ErrorService(I18NService i18NService, StyleService styleService) {
        this.i18NService = i18NService;
        this.styleService = styleService;
    }

    public void bindTitleCheck(TextField field, Label error) {
        applyValidation(field, error, minTitleLength, maxTitleLength);
    }

    public void bindCodeCheck(TextField field, Label error) {
        applyValidation(field, error, inviteCodeLength, inviteCodeLength);
    }

    public void bindPasswordCheck(TextField field, Label error) {
        applyValidation(field, error, passwordLength, passwordLength);
    }

    public void bindDescriptionCheck(TextField field, Label error) {
        applyValidation(field, error, minDescriptionLength, maxDescriptionLength);
    }

    public void bindFirstNameCheck(TextField field, Label error) {
        applyValidation(field, error, minFirstNameLength, maxGenericLength);
    }

    public void bindGenericCheck(TextField field, Label error) {
        applyValidation(field, error, 0, maxGenericLength);
    }

    public boolean validateTitle(TextField title, Label error) {
        return validateField(title, error, minTitleLength, maxTitleLength);
    }

    public boolean validateCode(TextField code, Label error) {
        return validateField(code, error, inviteCodeLength, inviteCodeLength);
    }

    public boolean validatePassword(TextField password, Label error) {
        return validateField(password, error, passwordLength, passwordLength);
    }

    public boolean validateFirstName(TextField firstName, Label error) {
        return validateField(firstName, error, minFirstNameLength, maxGenericLength);
    }

    /**
     * Validates the text field
     *
     * @param field field
     * @param error titleError
     * @return true if good
     */
    public boolean validateField(TextField field, Label error, int minLength, int maxLength) {
        final String resRes = validateText(field.getText(), minLength, maxLength);
        if (resRes.isEmpty()) return true;
        styleService.playFadeTransition(error, resRes);
        return false;
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
            if (!change.isAdded() && !change.isDeleted() && !change.isReplaced()) return change;
            String validation = validateText(change.getControlNewText(), minLength, maxLength);
            if (validation.isEmpty()) {
                i18NService.setText(error, "");
                return change;
            }
            styleService.playFadeTransition(error, validation);
            if (validation.equals(tooShort)) return change;
            return null;
        };
        input.setTextFormatter(new TextFormatter<>(filter));
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
        if (!validTextPattern.matcher(text).matches()) return invalidCharacters;
        if (text.length() > maxLength) return tooLong;
        if (text.length() < minLength) return tooShort;
        return "";
    }

    public void infoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        i18NService.setTranslation(alert, title, header, content);
        alert.showAndWait();
    }

    public void errorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        i18NService.setTranslation(alert, title, header, content);
        alert.showAndWait();
    }

    /**
     * Method for creating an alert for when the user enters the wrong password
     * for HomeOverviewCtrl
     *
     * @return the alert
     */
    public Alert wrongPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        i18NService.setTranslation(alert, "admin_login", "access_denied", "incorrect_password");
        return alert;
    }

    /**
     * Method for creating an alert for when the user enters the wrong inviteCode
     * for HomeOverviewCtrl
     *
     * @param e the exception thrown
     * @return the alert
     */
    public Alert eventCodeNotFound(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        if (e.getMessage().contains("Bad Request")) {
            alert.contentTextProperty().bind(i18NService.createStringBinding("code_404"));
        } else {
            alert.contentTextProperty().bind(i18NService.createStringBinding("code_error"));
        }
        return alert;
    }

    /**
     * Method for creating an alert for when the server connection fails
     * for mainCtrl
     *
     * @return the alert
     */
    public Alert serverConnectionError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        i18NService.setTranslation(alert, "connection.error", "connection.error.header");
        return alert;
    }

    /**
     * Method for creating an alert for when the user enters a good server url
     * for SettingsOverviewCtrl
     *
     * @param url the url entered by the user
     * @return the alert
     */
    public Alert successServerChange(String url) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        i18NService.setTranslation(alert, "url_changed", "url_changed_to", "success_change_url");
        alert.headerTextProperty().bind(i18NService.createStringBinding("url_changed_to", url));
        return alert;
    }

    /**
     * Method for creating an alert for when the user enters else rather than server url
     * for SettingsOverviewCtrl
     *
     * @param message the message to be displayed from the exception
     * @return the alert
     */
    public Alert wrongArgument(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.titleProperty().bind(i18NService.createStringBinding("wrong_input"));
        alert.setHeaderText(message);
        alert.contentTextProperty().bind(i18NService.createStringBinding("try_again"));
        return alert;
    }

    /**
     * Method for creating an alert for when an unknown exception is thrown after the server url is entered
     * for SettingsOverviewCtrl
     *
     * @return the alert
     */
    public Alert somethingWrong() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        i18NService.setTranslation(alert, "url_not_changed", "error_changing_url", "incorrect_url");
        return alert;
    }

    /**
     * Method for creating a confirmation alert for when the user wants to delete an event
     * for AdminOverviewCtrl
     *
     * @param inviteCode the invite code of the event to be deleted
     * @return the alert
     */
    public Alert confirmDeleteEvent(String inviteCode) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        i18NService.setTranslation(alert, "delEvent", "delHeader");
        alert.contentTextProperty().bind(i18NService.createStringBinding("are.you.sure.delete.event", inviteCode));
        return alert;
    }

    /**
     * Method for creating an alert for when the user tries to create an event
     * for addEventCtrl, addExpenseCtrl
     *
     * @param message the message to be displayed from the exception
     * @return the alert
     */
    public Alert generalError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setContentText(message);
        return alert;
    }

    /**
     * Method for creating an alert for when the user enters something else then numbers while making
     * an expense
     * for addExpenseCtrl
     *
     * @return the alert
     */
    public Alert numberFormatError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.contentTextProperty().bind(i18NService.createStringBinding("enter_amount"));
        return alert;
    }

    /**
     * Method for creating an alert for when the user tries to add a participant without firstname
     * for addParticipantCtrl
     *
     * @return the alert
     */
    public Alert noFirstName() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.contentTextProperty().bind(i18NService.createStringBinding("put_first_name"));
        return alert;
    }

    /**
     * Method for creating a confirmation alert for when the user tries to delete a participant from an event
     *
     * @param participant the participant to be deleted
     * @return the confirmation alert
     */
    public Alert confirmDeleteParticipant(Participant participant) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        i18NService.setTranslation(alert, "delete_participant", "delete_participant_confirmation");
//        alert.setContentText(map.get("first_name").toString() + participant.getFirstName() + "\n" + map.get("last_name").toString() + participant.getLastName() + "\n" + map.get("email").toString() + participant.getEmail() + "\n" + map.get("iban").toString() + participant.getIban() + "\n" + map.get("bic").toString() + participant.getBic()); ToDo
        return alert;
    }

    /**
     * Method for creating a confirmation alert for when the user tries to delete a payment or expense from an event
     *
     * @param isPayment boolean to check if it is a payment or expense
     * @param expense   the expense to be deleted or payment to be deleted
     * @return the confirmation alert
     */
    public Alert confirmDelete(boolean isPayment, Expense expense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (isPayment) alert.titleProperty().bind(i18NService.createStringBinding("delete_payment"));
        else alert.titleProperty().bind(i18NService.createStringBinding("delete_expense"));

        // ToDo
        alert.setHeaderText(i18NService.get("delete_this_confirmation") + " " +(isPayment ?
            i18NService.get("payment") : i18NService.get("expense")) + "?");
        alert.setContentText(expense.getTitle() + "\n" + expense.getAmount() + "\n" + expense.getDate());
        return alert;
    }

    /**
     * Creates an error alert for when something cannot be deleted
     *
     * @param titleCode  the error code to get the translation for the title
     * @param headerCode the error code to get the translation for the header
     * @return the alert created
     */
    public Alert cannotDelete(String titleCode, String headerCode) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        i18NService.setTranslation(alert, titleCode, headerCode);
        return alert;
    }

}
