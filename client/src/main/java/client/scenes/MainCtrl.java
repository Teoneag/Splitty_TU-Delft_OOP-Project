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

import client.services.*;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.inject.Inject;
import java.io.IOException;


public class MainCtrl {

    private Stage primaryStage;
    private final ConfigService configService;
    private final ErrorService errorService;
    private final EventService eventService;
    private final CurrencyService currencyService;
    private final I18NService i18NService;
    private final ServerUtils serverUtils;

    private Scene pageWithMenu;
    private PageWithMenuCtrl pageWithMenuCtrl;

    private Node home;
    private HomeCtrl homeCtrl;

    private Node event;
    private EventCtrl eventCtrl;

    private Node addExpense;
    private AddExpenseCtrl addExpenseCtrl;

    private Node addParticipant;
    private AddParticipantCtrl addParticipantCtrl;

    private Node admin;
    private AdminCtrl adminCtrl;

    private Node settings;
    private SettingsCtrl settingsCtrl;

    private Node shortcuts;
    private ShortcutsCtrl shortcutsPageCtrl;

    private Node debt;
    private DebtCtrl debtCtrl;

    private Node addTag;
    private AddTagCtrl addtagCtrl;

    private Node manageTags;
    private Statistics statistics;

    private Node addPayment;
    private AddPaymentCtrl addPaymentCtrl;

    public String email = "";

    @Inject
    public MainCtrl(ConfigService configService, EventService eventService, ErrorService errorService,
                    CurrencyService currencyService, I18NService i18NService, ServerUtils serverUtils) {
        this.configService = configService;
        this.errorService = errorService;
        this.eventService = eventService;
        this.currencyService = currencyService;
        this.i18NService = i18NService;
        this.serverUtils = serverUtils;
    }


    /**
     * @param primaryStage   primaryStage
     * @param home           overview
     * @param event          eventPage
     * @param addExpense     addExpense
     * @param addParticipant - addParticipant controller view pair
     * @param admin          - adminOverview controller view pair
     * @param settings       - settingsOverview controller view pair
     * @param shortcuts      - shortcuts controller view pair
     * @param debt           - debt controller view pair
     */

    public void initialize(Stage primaryStage, Pair<PageWithMenuCtrl, Parent> pageWithMenu, Pair<HomeCtrl, Parent> home, Pair<EventCtrl, Parent> event, Pair<AddExpenseCtrl, Parent> addExpense, Pair<AddParticipantCtrl, Parent> addParticipant, Pair<AdminCtrl, Parent> admin, Pair<SettingsCtrl, Parent> settings, Pair<ShortcutsCtrl, Parent> shortcuts, Pair<DebtCtrl, Parent> debt, Pair<AddTagCtrl, Parent> addTag, Pair<Statistics, Parent> manageTags, Pair<AddPaymentCtrl, Parent> addPayment) throws IOException {

        this.primaryStage = primaryStage;

        this.pageWithMenuCtrl = pageWithMenu.getKey();
        this.pageWithMenu = new Scene(pageWithMenu.getValue(), 1080, 720);

        this.homeCtrl = home.getKey();
        this.home = home.getValue();

        this.eventCtrl = event.getKey();
        this.event = event.getValue();

        this.addExpenseCtrl = addExpense.getKey();
        this.addExpense = addExpense.getValue();

        this.addParticipantCtrl = addParticipant.getKey();
        this.addParticipant = addParticipant.getValue();

        this.adminCtrl = admin.getKey();
        this.admin = admin.getValue();

        this.settingsCtrl = settings.getKey();
        this.settings = settings.getValue();

        this.shortcutsPageCtrl = shortcuts.getKey();
        this.shortcuts = shortcuts.getValue();

        this.debtCtrl = debt.getKey();
        this.debt = debt.getValue();

        this.addtagCtrl = addTag.getKey();
        this.addTag = addTag.getValue();

        this.statistics = manageTags.getKey();
        this.manageTags = manageTags.getValue();

        this.addPaymentCtrl = addPayment.getKey();
        this.addPayment = addPayment.getValue();

        // ToDo by using - mnemonic parsing and accelerator (add documentation + make them work for all pages and all languages)
//        KeyCombination backCombination = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
//        KeyCombination forwardCombination = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
//        KeyCombination ctrlP = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
//        KeyCombination ctrlE = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
//
//        final int MOUSE_BACK_BUTTON = MouseButton.BACK.ordinal();
//
//        showShortcuts(this.home);
//
//        addKeyEventFilter(this.event, backCombination, eventCtrl::goBack);
//        addKeyEventFilter(this.event, ctrlP, eventCtrl::addParticipant);
//        addKeyEventFilter(this.event, ctrlE, eventCtrl::addExpense);
//        addMouseEventFilter(this.event, MOUSE_BACK_BUTTON, eventCtrl::goBack);
//        showShortcuts(this.event);
//
//        addKeyEventFilter(this.addParticipant, backCombination, addParticipantCtrl::goBack);
//        addMouseEventFilter(this.addParticipant, MOUSE_BACK_BUTTON, addParticipantCtrl::goBack);
//        showShortcuts(this.addParticipant);
//
//        addKeyEventFilter(this.addExpense, backCombination, addExpenseCtrl::goBack);
//        addMouseEventFilter(this.addExpense, MOUSE_BACK_BUTTON, addExpenseCtrl::goBack);
//        showShortcuts(this.addExpense);
//
//        addKeyEventFilter(this.admin, backCombination, adminCtrl::goBack);
//        addMouseEventFilter(this.admin, MOUSE_BACK_BUTTON, adminCtrl::goBack);
//        showShortcuts(this.admin);
//
//        addKeyEventFilter(this.shortcuts, backCombination, shortcutsPageCtrl::goBack);
//        addMouseEventFilter(this.shortcuts, MOUSE_BACK_BUTTON, shortcutsPageCtrl::goBack);
//        showShortcuts(this.shortcuts);
//
//        addKeyEventFilter(this.settings, backCombination, settingsCtrl::goBack);
//        addMouseEventFilter(this.settings, MOUSE_BACK_BUTTON, settingsCtrl::goBack);
//        showShortcuts(this.settings);

        showPageWithMenu();
        showHome();
        primaryStage.show();

    }

    public void setTheme(String theme) {
        Platform.runLater(() -> pageWithMenu.getStylesheets().setAll("/stylesheets/" + theme + ".css"));
    }

    public void setCurrency(String currency) {
        configService.setConfigCurrency(currency);
        // ToDO update live currency for table
    }

    /**
     * Shows the shortcuts page
     *
     * @param scene the scene to show the shortcuts on
     */
    private void showShortcuts(Scene scene) {
        KeyCombination ctrlShiftSlash = new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        KeyCombination ctrlSlash = new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN);

        addKeyEventFilter(scene, ctrlShiftSlash, this::showShortcuts);
        addKeyEventFilter(scene, ctrlSlash, this::showShortcuts);
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

    public void showPageWithMenu() {
        i18NService.setLanguage(configService.getConfigLanguage());
        primaryStage.getIcons().add(new Image("images/Splitty_Icon.png"));
        primaryStage.setScene(pageWithMenu);
    }

    /**
     * Creates a popup to inform the user that the server is currently unavailable
     */
    public void serverConnectionAlert() {
        Alert alert = errorService.serverConnectionError();
        alert.showAndWait();
        i18NService.setText(primaryStage, "Splitty");
        pageWithMenuCtrl.setCenter(home);
    }

    /**
     * shows the home page
     * Set title to Splitty
     */
    public void showHome() {
        i18NService.setText(primaryStage, "splitty");
        pageWithMenuCtrl.setCenter(home, true);
        homeCtrl.focusTitleField();
        homeCtrl.refresh();
    }

    /**
     * Shows the settings overview
     */
    public void showSettings() {
        i18NService.setTranslation(primaryStage, "settings.title");
        pageWithMenuCtrl.setCenter(settings);
    }

    /**
     * Shows the shortcuts page
     */
    public void showShortcuts() {
        i18NService.setTranslation(primaryStage, "shortcuts.title");
        pageWithMenuCtrl.setCenter(shortcuts);
    }

    /**
     * Shows the admin overview
     */
    public void showAdminOverview() {
        i18NService.setTranslation(primaryStage, "admin.overview");
        adminCtrl.refresh();
        pageWithMenuCtrl.setCenter(admin);
    }

    /**
     * shows the page to add an event
     *
     * @param inviteCode the invite code of the event
     */
    public void showEvent(String inviteCode) {
        Event event = serverUtils.getEvent(inviteCode);
        showEvent(event);
    }

    /**
     * shows the page to add an event
     *
     * @param event the event to show
     */
    public void showEvent(Event event) {
        i18NService.setTranslation(primaryStage, "event.overview");
        eventService.addToRecentEvents(event.getInviteCode());
        if (!email.isEmpty()) {
            eventCtrl.showEmailSent(email);
            email = "";
        }
        eventCtrl.refresh(event);
        pageWithMenuCtrl.setCenter(this.event);
    }

    /**
     * Shows the adding participant screen and updates the controller to have the correct event
     *
     * @param event - The event to add a participant to
     */
    public void showAddParticipant(Event event) {
        i18NService.setTranslation(primaryStage, "add.participant");
        addParticipantCtrl.addParticipant(event);
        pageWithMenuCtrl.setCenter(addParticipant);
    }

    /**
     * Shows the adding participant screen but with the values filled in for the current participant
     *
     * @param event       The event the participant is in
     * @param participant The participant that is being edited
     */
    public void showEditParticipant(Event event, Participant participant) {
        i18NService.setTranslation(primaryStage, "edit.participant");
        addParticipantCtrl.edit(event, participant);
        pageWithMenuCtrl.setCenter(addParticipant);
    }

    /**
     * shows the page to add an expense
     *
     * @param event parent event
     */
    public void showAddExpense(Event event) {
        i18NService.setTranslation(primaryStage, "add.expense");
        addExpenseCtrl.refresh(event);
        pageWithMenuCtrl.setCenter(addExpense);
    }

    /**
     * shows the addExpense page but uses a reduced refresh method to preserve pre-tag creation values
     *
     * @param tag the newly added tag
     */
    public void showAddExpenseReduced(Tag tag) {
        i18NService.setTranslation(primaryStage, "add.expense");
        addExpenseCtrl.reducedRefresh(tag);
        pageWithMenuCtrl.setCenter(addExpense);
    }

    /**
     * shows the addExpense page but fills in the values from the current expense
     *
     * @param event   parent event
     * @param expense expense to edit
     */
    public void showEditExpense(Event event, Expense expense) {
        i18NService.setTranslation(primaryStage, "edit.expense");
        addExpenseCtrl.edit(event, expense);
        pageWithMenuCtrl.setCenter(addExpense);
    }

    public void showSettleDebt(Event event) {
        i18NService.setTranslation(primaryStage, "settle.debts");
        addPaymentCtrl.refresh(event, false);
        pageWithMenuCtrl.setCenter(addPayment);

    }

    /**
     * shows the debt overview for an event
     *
     * @param event event
     */
    public void showDebtOverview(Event event) {
        i18NService.setTranslation(primaryStage, "debt.overview");
        debtCtrl.refresh(event);
        pageWithMenuCtrl.setCenter(debt);
    }

    public void showAddTag(Event event, Expense expense) {
        i18NService.setTranslation(primaryStage, "add.tag");
        addtagCtrl.refreshEdit(event);
        pageWithMenuCtrl.setCenter(addTag);
    }

    public void showAddTagNoExpense(Event event) {
        i18NService.setTranslation(primaryStage, "add.tag");
        addtagCtrl.refresh(event);
        pageWithMenuCtrl.setCenter(addTag);
    }

    public void showManageTags(Event event) {
        i18NService.setTranslation(primaryStage, "statistics");
        statistics.refresh(event);
        pageWithMenuCtrl.setCenter(manageTags);
    }

    public void showEditTag(Event event, Tag tag) {
        i18NService.setTranslation(primaryStage, "edit.tag");
        addtagCtrl.editTag(event, tag);
        pageWithMenuCtrl.setCenter(addTag);
    }

    public void showAddPayment(Event event, boolean fromEvent) {
        i18NService.setTranslation(primaryStage, "add.payment");
        addPaymentCtrl.refresh(event, fromEvent);
        pageWithMenuCtrl.setCenter(addPayment);
    }

    public void showEditPayment(Event event, Expense expense) {
        i18NService.setTranslation(primaryStage, "edit.payment");
        addPaymentCtrl.edit(event, expense);
        pageWithMenuCtrl.setCenter(addPayment);
    }
}