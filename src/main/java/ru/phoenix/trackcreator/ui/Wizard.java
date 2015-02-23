package ru.phoenix.trackcreator.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Stack;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class Wizard extends StackPane {
    private static final int UNDEFINED = -1;
    private ObservableList<WizardPage> pages = FXCollections.observableArrayList();
    private Stack<Integer> history = new Stack<Integer>();
    private int curPageIdx = UNDEFINED;

    protected Stage owner;

    public Wizard(WizardPage... nodes) {
        this.setStyle("-fx-padding: 10;");
        pages.addAll(nodes);
        navTo(0);
    }

    public Stage getStage() {
        return owner;
    }

    void nextPage() {
        if (hasNextPage()) {
            navTo(curPageIdx + 1);
        }
    }

    void priorPage() {
        if (hasPriorPage()) {
            navTo(history.pop(), false);
        }
    }

    boolean hasNextPage() {
        return (curPageIdx < pages.size() - 1);
    }

    boolean hasPriorPage() {
        return !history.isEmpty();
    }

    void navTo(int nextPageIdx, boolean pushHistory) {
        if (nextPageIdx < 0 || nextPageIdx >= pages.size())
            return;
        if (curPageIdx != UNDEFINED) {
            if (pushHistory) {
                history.push(curPageIdx);
            }
        }

        WizardPage nextPage = pages.get(nextPageIdx);
        curPageIdx = nextPageIdx;
        getChildren().clear();
        getChildren().add(nextPage);
        nextPage.manageButtons();
        nextPage.onForegraund();
    }

    void navTo(int nextPageIdx) {
        navTo(nextPageIdx, true);
    }

    public void finish() {
    }
}
