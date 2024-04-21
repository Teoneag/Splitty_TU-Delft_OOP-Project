package client.services;

import javafx.beans.property.SimpleStringProperty;

public class Shortcut {
    private final SimpleStringProperty action, pageForAction, shortcut1, shortcut2, shortcut3;

    /**
     * Constructor for the Shortcut class
     * @param action action string
     * @param pageForAction page for action string
     * @param shortcut1 shortcut1 string
     * @param shortcut2 shortcut2 string
     * @param shortcut3 shortcut3 string
     */
    public Shortcut(String action, String pageForAction, String shortcut1, String shortcut2, String shortcut3) {
        this.action = new SimpleStringProperty(action);
        this.pageForAction = new SimpleStringProperty(pageForAction);
        this.shortcut1 = new SimpleStringProperty(shortcut1);
        this.shortcut2 = new SimpleStringProperty(shortcut2);
        this.shortcut3 = new SimpleStringProperty(shortcut3);
    }

    public String getAction() {
        return action.get();
    }

    public SimpleStringProperty actionProperty() {
        return action;
    }

    public String getPageForAction() {
        return pageForAction.get();
    }

    public SimpleStringProperty pageForActionProperty() {
        return pageForAction;
    }

    public String getShortcut1() {
        return shortcut1.get();
    }

    public SimpleStringProperty shortcut1Property() {
        return shortcut1;
    }

    public String getShortcut2() {
        return shortcut2.get();
    }

    public SimpleStringProperty shortcut2Property() {
        return shortcut2;
    }

    public String getShortcut3() {
        return shortcut3.get();
    }

    public SimpleStringProperty shortcut3Property() {
        return shortcut3;
    }
}