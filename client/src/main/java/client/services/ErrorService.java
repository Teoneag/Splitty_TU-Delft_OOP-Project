package client.services;

import commons.Expense;
import commons.Participant;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ErrorService {
    private HashMap<String, Object> map;

    public ErrorService() {
        this.map = new HashMap<>();
    }

    public void changeLanguage(HashMap<String, Object> map2) {
        this.map = map2;
    }

    /**
     * Method for creating an alert for when the user enters the wrong password
     * for HomeoverviewCtrl
     *
     * @param result the result of the login attempt
     * @return the alert
     */
    public Alert wrongPassword(String result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(map.get("adminLoginAlertTitle").toString());
        alert.setHeaderText(map.get("adminLoginHeader").toString());
        result = map.get("adminLoginContent").toString();
        alert.setContentText(result);
        return alert;
    }

    /**
     * Method for creating an alert for when the user enters the wrong invitecode
     * for HomeoverviewCtrl
     *
     * @param inviteCode the invite code entered by the user
     * @return the alert
     */
    public Alert joinCodeLength(String inviteCode) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setContentText(
            map.get("inviteCodeLength").toString());
        if (inviteCode.length() < 6) {
            alert.setHeaderText(map.get("inviteCodeShortHeader").toString());
        } else {
            alert.setHeaderText(map.get("inviteCodeLongHeader").toString());
        }
        return alert;
    }

    /**
     * Method for creating an alert for when the user enters the wrong invitecode
     * for HomeoverviewCtrl
     *
     * @param e the exception thrown
     * @return the alert
     */
    public Alert eventCodeNotFound(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        if (e.getMessage().contains("Bad Request")) {
            alert.setContentText(map.get("eventCodeNotFound").toString());
        } else {
            alert.setContentText(map.get("eventCodeNotFound2").toString());
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
        alert.setTitle(map.get("connectionError").toString());
        alert.setHeaderText(map.get("connectionErrorHeader").toString());
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
        alert.setTitle(map.get("serverSuccessTitle").toString());
        alert.setHeaderText(map.get("serverHeader").toString() + url);
        alert.setContentText(map.get("serverContent").toString());
        return alert;
    }

    /**
     * Method for creating an alert for when the user enters else rather then server url
     * for SettingsOverviewCtrl
     *
     * @param message the message to be displayed from the exception
     * @return the alert
     */
    public Alert wrongArgument(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(map.get("wrongArgument").toString());
        alert.setHeaderText(message);
        alert.setContentText(map.get("wrongContent").toString());
        return alert;
    }

    /**
     * Method for creating an alert for when an unknown exepction is thrown after the server url is entered
     * for SettingsOverviewCtrl
     *
     * @return the alert
     */
    public Alert somethingWrong() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(map.get("serverErrorTitle").toString());
        alert.setHeaderText(map.get("serverErrorHeader").toString());
        alert.setContentText(map.get("serverErrorContent").toString());
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
        alert.setTitle(map.get("delEvent").toString());
        alert.setHeaderText(map.get("delHeader").toString());
        alert.setContentText(map.get("delContext").toString() + inviteCode);
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
        alert.setContentText(map.get("noNumbers").toString());
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
        alert.setContentText(map.get("noFirstName").toString());
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
        alert.setTitle(map.get("delParticipant").toString());
        alert.setHeaderText(map.get("delParticipantHeader").toString());
        alert.setContentText(map.get("first_name").toString() + participant.getFirstName() +
            "\n" + map.get("last_name").toString() +
            participant.getLastName() + "\n" + map.get("email").toString() +
            participant.getEmail() + "\n" +
            map.get("iban").toString() +
            participant.getIban() + "\n" + map.get("bic").toString() + participant.getBic());
        return alert;
    }

    /**
     * Method for creating a confirmation alert for when the user tries to delete a payment or expense from an event
     * @param isPayment boolean to check if it is a payment or expense
     * @param expense the expense to be deleted or payment to be deleted
     * @return the confirmation alert
     */
    public Alert confirmDelete(boolean isPayment, Expense expense){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(isPayment ?  map.get("delPayment").toString() : map.get("delExpense").toString());
        alert.setHeaderText(map.get("delHeader2").toString() +
            (isPayment ? map.get("payment").toString() : map.get("expense").toString()) + "?");
        alert.setContentText(expense.getTitle() + "\n" + expense.getAmount() + "\n" + expense.getDate());
        return alert;
    }
    
    /**
     * Creates an error alert for when something cannot be deleted
     * @param titleCode the error code to get the translation for the title
     * @param headerCode the error code to get the translation for the header
     * @return the alert created
     */
    public Alert cannotDelete(String titleCode, String headerCode) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(map.get(titleCode).toString());
        alert.setHeaderText(map.get(headerCode).toString());
        return alert;
    }

}
