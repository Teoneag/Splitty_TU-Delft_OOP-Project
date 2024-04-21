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
package client;

import client.scenes.*;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.google.inject.Guice.createInjector;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    /**
     * @param args args
     * @throws URISyntaxException exception
     * @throws IOException        exception
     */
    public static void main(String[] args) throws URISyntaxException, IOException {
        launch();
    }

    /**
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws IOException exception
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        var addEventOverview = FXML.load(AddEventCtrl.class, "client", "scenes", "addEvent.fxml");
        var addExpenseOverview =
            FXML.load(AddExpenseCtrl.class, "client", "scenes", "addExpense.fxml");
        var addParticipantOverview =
            FXML.load(AddParticipantCtrl.class, "client", "scenes", "AddParticipant.fxml");
        var homeOverview =
            FXML.load(HomeOverviewCtrl.class, "client", "scenes", "HomeOverview.fxml");
        var eventOverview =
            FXML.load(EventOverviewCtrl.class, "client", "scenes", "EventOverview.fxml");
        var adminOverview =
            FXML.load(AdminOverviewCtrl.class, "client", "scenes", "AdminOverview.fxml");
        var settingsOverview =
            FXML.load(SettingsOverviewCtrl.class, "client", "scenes", "SettingsOverview.fxml");
        var shortcutsPageOverview =
            FXML.load(ShortcutsCtrl.class, "client", "scenes", "Shortcuts.fxml");
        var debtOverview =
            FXML.load(DebtOverviewCtrl.class, "client", "scenes", "DebtOverview.fxml");
        var addTagOverview =
            FXML.load(AddTagCtrl.class, "client", "scenes", "addTag.fxml");
        var manageTagOverview =
            FXML.load(ManageTagsCtrl.class, "client", "scenes", "manageTags.fxml");
        var addPaymentOverview =
            FXML.load(AddPaymentCtrl.class, "client", "scenes", "addPayment.fxml");

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        
        primaryStage.setOnCloseRequest(e -> adminOverview.getKey().stop());

        mainCtrl.initialize(primaryStage,
            homeOverview,
            addEventOverview,
            eventOverview,
            addExpenseOverview,
            addParticipantOverview,
            adminOverview,
            settingsOverview,
            shortcutsPageOverview,
            debtOverview,
            addTagOverview,
            manageTagOverview,
            addPaymentOverview
        );

    }
}