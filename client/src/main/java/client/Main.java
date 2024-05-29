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
    public static void main(String[] args) throws Exception {
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
        var pageWithMenu = FXML.load(PageWithMenuCtrl.class, "widgets", "PageWithMenu.fxml");
        var home = FXML.load(HomeCtrl.class, "client", "scenes", "Home.fxml");
        var settings = FXML.load(SettingsCtrl.class, "client", "scenes", "Settings.fxml");
        var shortcuts = FXML.load(ShortcutsCtrl.class, "client", "scenes", "Shortcuts.fxml");
        var admin = FXML.load(AdminCtrl.class, "client", "scenes", "Admin.fxml");
        var event = FXML.load(EventCtrl.class, "client", "scenes", "Event.fxml");
        var addExpense = FXML.load(AddExpenseCtrl.class, "client", "scenes", "AddExpense.fxml");
        var addParticipant = FXML.load(AddParticipantCtrl.class, "client", "scenes", "AddParticipant.fxml");
        var debt = FXML.load(DebtCtrl.class, "client", "scenes", "Debt.fxml");
        var addTag = FXML.load(AddTagCtrl.class, "client", "scenes", "AddTag.fxml");
        var manageTag = FXML.load(Statistics.class, "client", "scenes", "Statistics.fxml");
        var addPayment = FXML.load(AddPaymentCtrl.class, "client", "scenes", "AddPayment.fxml");

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);

        primaryStage.setOnCloseRequest(e -> admin.getKey().stop());

        mainCtrl.initialize(primaryStage, pageWithMenu, home, event, addExpense, addParticipant, admin, settings, shortcuts, debt, addTag, manageTag, addPayment);

    }
}