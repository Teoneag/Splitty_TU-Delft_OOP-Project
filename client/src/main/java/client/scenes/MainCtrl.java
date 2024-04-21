/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.services.ConfigService;
import client.services.ErrorService;
import client.services.EventService;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;


public class MainCtrl {

    private Stage primaryStage;
    private final ConfigService configService;
    private final ErrorService errorService;
    private final EventService eventService;

    private Scene homePageOverview;
    private HomeOverviewCtrl homeCtrl;

    private Scene addEventOverview;
    private AddEventCtrl addEventCtrl;

    private Scene eventOverview;
    private EventOverviewCtrl eventCtrl;

    private Scene addExpenseOverview;
    private AddExpenseCtrl addExpenseCtrl;

    private Scene addParticipantOverview;
    private AddParticipantCtrl addParticipantCtrl;

    private Scene adminOverview;
    private AdminOverviewCtrl adminCtrl;

    private Scene settingsOverview;
    private SettingsOverviewCtrl settingsCtrl;

    private Scene shortcutsPageOverview;
    private ShortcutsCtrl shortcutsPageCtrl;

    private Scene debtOverview;
    private DebtOverviewCtrl debtOverviewCtrl;

    private Scene addTagOverview;
    private AddTagCtrl addtagCtrl;
    private HashMap<String, Object> map;

    private Scene manageTagsOverview;
    private ManageTagsCtrl manageTagsCtrl;

    private Scene addPaymentOverview;
    private AddPaymentCtrl addPaymentCtrl;

    @Inject
    public MainCtrl(ConfigService configService, EventService eventService) {
        this.configService = configService;
        this.errorService = new ErrorService();
        this.eventService = eventService;
    }


    /**
     * @param primaryStage     primaryStage
     * @param overview         overview
     * @param addEvent         addEvent
     * @param eventPage        eventPage
     * @param addExpense       addExpense
     * @param addParticipant   - addParticipant controller view pair
     * @param adminOverview    - adminOverview controller view pair
     * @param settingsOverview - settingsOverview controller view pair
     * @param shortcuts        - shortcuts controller view pair
     * @param debtOverview     - debt controller view pair
     */

    public void initialize(Stage primaryStage, Pair<HomeOverviewCtrl, Parent> overview,
                           Pair<AddEventCtrl, Parent> addEvent,
                           Pair<EventOverviewCtrl, Parent> eventPage,
                           Pair<AddExpenseCtrl, Parent> addExpense,
                           Pair<AddParticipantCtrl, Parent> addParticipant,
                           Pair<AdminOverviewCtrl, Parent> adminOverview,
                           Pair<SettingsOverviewCtrl, Parent> settingsOverview,
                           Pair<ShortcutsCtrl, Parent> shortcuts,
                           Pair<DebtOverviewCtrl, Parent> debtOverview,
                           Pair<AddTagCtrl, Parent> addTagOverview,
                           Pair<ManageTagsCtrl, Parent> manageTagsOverview,
                           Pair<AddPaymentCtrl, Parent> addPaymentOverview) throws IOException {

        this.primaryStage = primaryStage;

        this.eventCtrl = eventPage.getKey();
        this.eventOverview = new Scene(eventPage.getValue());

        this.homeCtrl = overview.getKey();
        this.homePageOverview = new Scene(overview.getValue());

        this.addEventCtrl = addEvent.getKey();
        this.addEventOverview = new Scene(addEvent.getValue());

        this.addExpenseCtrl = addExpense.getKey();
        this.addExpenseOverview = new Scene(addExpense.getValue());

        this.addParticipantCtrl = addParticipant.getKey();
        this.addParticipantOverview = new Scene(addParticipant.getValue());

        this.adminCtrl = adminOverview.getKey();
        this.adminOverview = new Scene(adminOverview.getValue());

        this.settingsOverview = new Scene(settingsOverview.getValue());
        this.settingsCtrl = settingsOverview.getKey();

        this.shortcutsPageCtrl = shortcuts.getKey();
        this.shortcutsPageOverview = new Scene(shortcuts.getValue());

        this.debtOverviewCtrl = debtOverview.getKey();
        this.debtOverview = new Scene(debtOverview.getValue());

        this.addtagCtrl = addTagOverview.getKey();
        this.addTagOverview = new Scene(addTagOverview.getValue());

        this.manageTagsCtrl = manageTagsOverview.getKey();
        this.manageTagsOverview = new Scene(manageTagsOverview.getValue());

        this.addPaymentCtrl = addPaymentOverview.getKey();
        this.addPaymentOverview = new Scene(addPaymentOverview.getValue());

        KeyCombination backCombination = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
        KeyCombination forwardCombination = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN); // TODO
        KeyCombination ctrlP = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrlE = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);

        final int MOUSE_BACK_BUTTON = MouseButton.BACK.ordinal();

        showShortcuts(homePageOverview);

        addKeyEventFilter(addEventOverview, backCombination, addEventCtrl::goBack);
        addMouseEventFilter(addEventOverview, MOUSE_BACK_BUTTON, addEventCtrl::goBack);
        showShortcuts(addEventOverview);

        addKeyEventFilter(eventOverview, backCombination, eventCtrl::goBack);
        addKeyEventFilter(eventOverview, ctrlP, eventCtrl::addParticipant);
        addKeyEventFilter(eventOverview, ctrlE, eventCtrl::addExpense);
        addMouseEventFilter(eventOverview, MOUSE_BACK_BUTTON, eventCtrl::goBack);
        showShortcuts(eventOverview);

        addKeyEventFilter(addParticipantOverview, backCombination, addParticipantCtrl::goBack);
        addMouseEventFilter(addParticipantOverview, MOUSE_BACK_BUTTON, addParticipantCtrl::goBack);
        showShortcuts(addParticipantOverview);

        addKeyEventFilter(addExpenseOverview, backCombination, addExpenseCtrl::goBack);
        addMouseEventFilter(addExpenseOverview, MOUSE_BACK_BUTTON, addExpenseCtrl::goBack);
        showShortcuts(addExpenseOverview);

        addKeyEventFilter(this.adminOverview, backCombination, adminCtrl::goBack);
        addMouseEventFilter(this.adminOverview, MOUSE_BACK_BUTTON, adminCtrl::goBack);
        showShortcuts(this.adminOverview);

        addKeyEventFilter(this.shortcutsPageOverview, backCombination, shortcutsPageCtrl::goBack);
        addMouseEventFilter(this.shortcutsPageOverview, MOUSE_BACK_BUTTON, shortcutsPageCtrl::goBack);
        showShortcuts(this.shortcutsPageOverview);

        switchLanguage();
        addKeyEventFilter(this.settingsOverview, backCombination, settingsCtrl::goBack);
        addMouseEventFilter(this.settingsOverview, MOUSE_BACK_BUTTON, settingsCtrl::goBack);
        showShortcuts(this.settingsOverview);

        showOverview();
        primaryStage.show();
    }

    public void setTheme(String theme) {
        String themeA = "client/scenes/src/main/java/client/stylesheet/SplittyStylesheet.css";
        if(theme.equals("contrast")) {
            themeA = "client/scenes/src/main/java/client/stylesheet/contrastSheet.css";
        }
        setStylesheet(themeA);
    }

    public void setStylesheet(String theme) {
        primaryStage.getScene().getStylesheets().clear();
        primaryStage.getScene().getStylesheets().add(theme);
//        settingsOverview.getStylesheets().set(0, theme);
    }

    /**
     * Switches the language of the application
     */
    public void switchLanguage() {
        this.map = configService.getLanguage();
        String language = configService.getConfigLanguage();
        errorService.changeLanguage(this.map);

        settingsCtrl.setLanguage(map, language);
        shortcutsPageCtrl.setLanguage(map);
        addEventCtrl.setLanguage(map);
        addExpenseCtrl.setLanguage(map);
        addParticipantCtrl.setLanguage(map);
        adminCtrl.setLanguage(map, language);
        eventCtrl.setLanguage(map, language);
        homeCtrl.setLanguage(map, language);
        debtOverviewCtrl.setLanguage(map);
        addPaymentCtrl.setLanguage(map);
        addtagCtrl.setLanguage(map);
        manageTagsCtrl.setLanguage(map);
//        manageTagsCtrl.setPieChartTitleLanguage(map);
    }

    /**
     * Set the size of a button based on the length of the text
     *
     * @param button the button to set the size of
     */
    public void setDynamicButtonSize(Button button) {
        button.setPrefWidth(button.getText().length() * 14); // Adjust multiplier for appropriate scaling
    }


    /**
     * Shows the shortcuts page
     *
     * @param scene the scene to show the shortcuts on
     */
    private void showShortcuts(Scene scene) {
        KeyCombination ctrlShiftSlash = new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        KeyCombination ctrlSlash = new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN);

        addKeyEventFilter(scene, ctrlShiftSlash, homeCtrl::showShortcuts);
        addKeyEventFilter(scene, ctrlSlash, homeCtrl::showShortcuts);
    }

    /**
     * Adds a key event filter to a node
     *
     * @param node           the node to add the filter to
     * @param keyCombination the key combination to filter for
     * @param action         the action to run when the key combination is pressed
     */
    private void addKeyEventFilter(Scene node, KeyCombination keyCombination, Runnable action) {
        node.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (keyCombination.match(event)) {
                action.run();
                event.consume();
            }
        });
    }

    /**
     * Adds a mouse event filter to a node
     *
     * @param node               the node to add the filter to
     * @param mouseButtonOrdinal the ordinal of the mouse button to filter for
     * @param action             the action to run when the mouse button is pressed
     */
    private void addMouseEventFilter(Scene node, int mouseButtonOrdinal, Runnable action) {
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton().ordinal() == mouseButtonOrdinal) {
                action.run();
                event.consume();
            }
        });
    }


    /**
     * shows the home page
     * Set title to Splitty
     */
    public void showOverview() {
        primaryStage.setTitle("Splitty");
        primaryStage.getIcons().add(new Image("images/Splitty_Icon.png"));
        primaryStage.setScene(homePageOverview);
        homeCtrl.refresh();
    }

    public void showNoConnectionOverview() {
        primaryStage.setTitle("Splitty");
        primaryStage.setScene(homePageOverview);
    }

    /**
     * shows the page to add an event
     *
     * @param inviteCode the invite code of the event
     */
    public void showEventOverview(String inviteCode) {
        try {
            eventService.addToRecentEvents(inviteCode);
            eventCtrl.refresh(inviteCode);
            primaryStage.setTitle(map.get("eventTitle").toString());
            primaryStage.setScene(eventOverview);
        } catch (Exception e) {
            // ToDo: show Error: e.getMessage()
        }

    }

    /**
     * shows the page to add an event
     *
     * @param event the event to show
     */
    public void showEventOverview(Event event) {
        eventService.addToRecentEvents(event.getInviteCode());
        eventCtrl.refresh(event);
        primaryStage.setTitle(map.get("eventTitle").toString());
        primaryStage.setScene(eventOverview);
    }

    /**
     * When called changes the shown scene to the "addEventOverview" scene
     */
    public void showAddEvent() {
        primaryStage.setTitle(map.get("createEventTitle").toString());
        primaryStage.setScene(addEventOverview);
    }

    /**
     * shows the page to add an expense
     *
     * @param event parent event
     */
    public void showAddExpense(Event event) {
        addExpenseCtrl.refresh(event);
        primaryStage.setTitle(map.get("createExpenseTitle").toString());
        primaryStage.setScene(addExpenseOverview);
    }
    
    public void showSettleDebt(Event event) {
        addPaymentCtrl.refresh(event, false);
        primaryStage.setTitle("Settling Debts");
        primaryStage.setScene(addPaymentOverview);
        
    }


    /**
     * shows the addExpense page but fills in the values from the current expense
     *
     * @param event   parent event
     * @param expense expense to edit
     */
    public void showEditExpense(Event event, Expense expense) {
        addExpenseCtrl.edit(event, expense);
        primaryStage.setTitle(map.get("expenseEditTitle").toString());
        primaryStage.setScene(addExpenseOverview);
    }

    /**
     * shows the addExpense page but uses a reduced refresh method to preserve pre-tag creation values
     *
     * @param tag the newly added tag
     */
    public void showAddExpenseReduced(Tag tag) {
        addExpenseCtrl.reducedRefresh(tag);
        primaryStage.setTitle(map.get("createExpenseTitle").toString());
        primaryStage.setScene(addExpenseOverview);
    }


    /**
     * Shows the adding participant screen and updates the controller to have the correct event
     *
     * @param event - The event to add a participant to
     */
    public void showAddParticipant(Event event) {
        addParticipantCtrl.addParticipant(event);
        primaryStage.setTitle(map.get("createParticipantTitle").toString());
        primaryStage.setScene(addParticipantOverview);
    }

    /**
     * Shows the adding participant screen but with the values filled in for the current participant
     *
     * @param event       The event the participant is in
     * @param participant The participant that is being edited
     */
    public void showEditParticipant(Event event, Participant participant) {
        addParticipantCtrl.edit(event, participant);
        primaryStage.setTitle(map.get("participantEditTitle").toString());
        primaryStage.setScene(addParticipantOverview);
    }

    /**
     * Shows the admin overview
     */
    public void showAdminOverview() {
        adminCtrl.refresh();
        primaryStage.setTitle(map.get("adminTitle").toString());
        primaryStage.setScene(adminOverview);
    }

    /**
     * Shows the settings overview
     */
    public void showSettingsOverview() {
        primaryStage.setTitle(map.get("settingsTitle").toString());
        primaryStage.setScene(settingsOverview);
    }

    /**
     * Shows the shortcuts page
     */
    public void showShortcuts() {
        primaryStage.setTitle(map.get("shortcutTitle").toString());
        primaryStage.setScene(shortcutsPageOverview);
    }

    /**
     * shows the debt overview for an event
     *
     * @param event event
     */
    public void showDebtOverview(Event event) {
        primaryStage.setTitle(map.get("debtTitle").toString());
        debtOverviewCtrl.refresh(event);
        primaryStage.setScene(debtOverview);
    }

    /**
     * Creates a popup to inform the user that the server is currently unavailable
     */
    public void serverConnectionAlert() {
        Alert alert = errorService.serverConnectionError();
        alert.showAndWait();
        showNoConnectionOverview();
    }

    public void showAddTag(Event event, Expense expense) {
        primaryStage.setTitle("Splitty");
        addtagCtrl.refreshEdit(event, expense);
        primaryStage.setScene(addTagOverview);
    }
    public void showAddTagNoExpense(Event event) {
        primaryStage.setTitle("Splitty");
        addtagCtrl.refresh(event);
        primaryStage.setScene(addTagOverview);
    }

    public void showManageTags(Event event) {
        primaryStage.setTitle("Splitty");
        manageTagsCtrl.refresh(event);
        primaryStage.setScene(manageTagsOverview);
    }

    public void showEditTag(Event event, Tag tag) {
        addtagCtrl.editTag(event, tag);
        primaryStage.setScene(addTagOverview);
    }

    public void showAddPayment(Event event, boolean fromEvent) {
        primaryStage.setTitle("Add Payment");
        addPaymentCtrl.refresh(event, fromEvent);
        primaryStage.setScene(addPaymentOverview);
    }

    public void showEditPayment(Event event, Expense expense) {
        primaryStage.setTitle("Edit Payment");
        addPaymentCtrl.edit(event, expense);
        primaryStage.setScene(addPaymentOverview);
    }
}