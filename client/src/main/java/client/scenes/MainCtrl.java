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
import java.util.Stack;


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
    private final Stack<NavigationEntry> navigationHistory = new Stack<>();

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

        setShortcuts();

        showPageWithMenu();
        showHome();
        primaryStage.show();
    }

    private void setShortcuts() {
        KeyCombination backCombination = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
        KeyCombination ctrlShiftSlash = new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        KeyCombination ctrlSlash = new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN);
        this.pageWithMenu.setOnKeyPressed(e -> {
            if (backCombination.match(e)) {
                pageWithMenuCtrl.backToHome();
            } else if (ctrlShiftSlash.match(e) || ctrlSlash.match(e)) {
                showShortcuts();
            }
        });

        this.pageWithMenu.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton().ordinal() == MouseButton.BACK.ordinal()) {
                pageWithMenuCtrl.backToHome();
                e.consume();
            }
        });

        KeyCombination ctrlP = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrlE = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
        this.event.setOnKeyPressed(e -> {
            if (ctrlP.match(e)) {
                eventCtrl.addParticipant();
            } else if (ctrlE.match(e)) {
                eventCtrl.addExpense();
            }
        });
//        KeyCombination forwardCombination = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
    }

    public void navigateTo(Node node, String title) {
        i18NService.setTranslation(primaryStage, title);
        navigationHistory.push(new NavigationEntry(node, title));
        pageWithMenuCtrl.setCenter(node);
        if (navigationHistory.size() > 1) pageWithMenuCtrl.showBackButton();
    }

    public void goBack() {
        if (navigationHistory.size() == 1) return;

        navigationHistory.pop();
        NavigationEntry entry = navigationHistory.peek();
        i18NService.setTranslation(primaryStage, entry.title());
        pageWithMenuCtrl.setCenter(entry.node());
        if (navigationHistory.size() == 1) pageWithMenuCtrl.hideBackButton();
    }

    public void setTheme(String theme) {
        Platform.runLater(() -> pageWithMenu.getStylesheets().setAll("/stylesheets/" + theme + ".css"));
    }

    public void setCurrency(String currency) {
        configService.setConfigCurrency(currency);
        // ToDO update live currency for table
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
        navigateTo(home, "splitty.home");
        homeCtrl.focusTitleField();
        homeCtrl.refresh();
    }

    /**
     * Shows the settings overview
     */
    public void showSettings() {
        navigateTo(settings, "settings.title");
    }

    /**
     * Shows the shortcuts page
     */
    public void showShortcuts() {
        navigateTo(shortcuts, "shortcuts.title");
    }

    /**
     * Shows the admin overview
     */
    public void showAdminOverview() {
        navigateTo(admin, "admin.overview");
        adminCtrl.refresh();
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
        eventService.addToRecentEvents(event.getInviteCode());
        if (!email.isEmpty()) {
            eventCtrl.showEmailSent(email);
            email = "";
        }
        eventCtrl.refresh(event);
        navigateTo(this.event, "event.overview");
    }

    /**
     * Shows the adding participant screen and updates the controller to have the correct event
     *
     * @param event - The event to add a participant to
     */
    public void showAddParticipant(Event event) {
        navigateTo(addParticipant, "add.participant");
        addParticipantCtrl.addParticipant(event);
    }

    /**
     * Shows the adding participant screen but with the values filled in for the current participant
     *
     * @param event       The event the participant is in
     * @param participant The participant that is being edited
     */
    public void showEditParticipant(Event event, Participant participant) {
        navigateTo(addParticipant, "edit.participant");
        addParticipantCtrl.edit(event, participant);
    }

    /**
     * shows the page to add an expense
     *
     * @param event parent event
     */
    public void showAddExpense(Event event) {
        navigateTo(addExpense, "add.expense");
        addExpenseCtrl.refresh(event);
    }

    /**
     * shows the addExpense page but uses a reduced refresh method to preserve pre-tag creation values
     *
     * @param tag the newly added tag
     */
    public void showAddExpenseReduced(Tag tag) {
        navigateTo(addExpense, "add.expense");
        addExpenseCtrl.reducedRefresh(tag);
    }

    /**
     * shows the addExpense page but fills in the values from the current expense
     *
     * @param event   parent event
     * @param expense expense to edit
     */
    public void showEditExpense(Event event, Expense expense) {
        navigateTo(addExpense, "edit.expense");
        addExpenseCtrl.edit(event, expense);
    }

    public void showSettleDebt(Event event) {
        navigateTo(addPayment, "settle.debts");
        addPaymentCtrl.refresh(event, false);

    }

    /**
     * shows the debt overview for an event
     *
     * @param event event
     */
    public void showDebtOverview(Event event) {
        navigateTo(debt, "debt.overview");
        debtCtrl.refresh(event);
    }

    public void showAddTag(Event event, Expense expense) {
        navigateTo(addTag, "add.tag");
        addtagCtrl.refreshEdit(event);
    }

    public void showAddTagNoExpense(Event event) {
        navigateTo(addTag, "add.tag");
        addtagCtrl.refresh(event);
    }

    public void showManageTags(Event event) {
        navigateTo(manageTags, "statistics");
        statistics.refresh(event);
    }

    public void showEditTag(Event event, Tag tag) {
        navigateTo(addTag, "edit.tag");
        addtagCtrl.editTag(event, tag);
    }

    public void showAddPayment(Event event, boolean fromEvent) {
        navigateTo(addPayment, "add.payment");
        addPaymentCtrl.refresh(event, fromEvent);
    }

    public void showEditPayment(Event event, Expense expense) {
        navigateTo(addPayment, "edit.payment");
        addPaymentCtrl.edit(event, expense);
    }
}

record NavigationEntry(Node node, String title) {
}