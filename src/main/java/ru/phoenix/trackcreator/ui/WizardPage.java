package ru.phoenix.trackcreator.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public abstract class WizardPage extends VBox {
    protected Button priorButton = new Button("_Предыдущая");
    protected Button nextButton = new Button("_Следующая");
    protected Button finishButton = new Button("_Закрыть");

    private ProgressDialog progressDialog;

    public WizardPage(String title) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 5 0;");
        getChildren().add(label);
        setId(title);
        setSpacing(5);
        setStyle("-fx-padding:10; -fx-border-width: 3;");

        try {
            Region spring = new Region();
            VBox.setVgrow(spring, Priority.ALWAYS);
            getChildren().addAll(getContent(), spring, getButtons());
        } catch (IOException e) {
            e.printStackTrace();
        }

        priorButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                priorPage();
            }
        });
        nextButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                nextPage();
            }
        });
        finishButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                getWizard().finish();
            }
        });
    }

    public HBox getButtons() {
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        HBox buttonBar = new HBox(5);
        buttonBar.getChildren().addAll(spring, priorButton, nextButton, finishButton);
        return buttonBar;
    }

    public abstract Parent getContent() throws IOException;

    public boolean hasNextPage() {
        return getWizard().hasNextPage();
    }

    public boolean hasPriorPage() {
        return getWizard().hasPriorPage();
    }

    public void nextPage() {
        getWizard().nextPage();
    }

    public void priorPage() {
        getWizard().priorPage();
    }

    public Wizard getWizard() {
        return (Wizard) getParent();
    }

    public void manageButtons() {
        if (!hasPriorPage()) {
            priorButton.setDisable(true);
        }

        if (!hasNextPage()) {
            nextButton.setDisable(true);
        }
    }

    public void onForegraund() {

    }

    protected void viewAlertDialog(String title, String message) {
        try {
            closeProgressDialog();
            new AlertDialog(title, message).show(getWizard().getStage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void viewProgressDialog(String message) {
        if (progressDialog != null) {
            progressDialog.setMessage(message);
        } else {
            try {
                progressDialog = new ProgressDialog(message);
                progressDialog.show(getWizard().getStage());
            } catch (IOException e) {
                progressDialog = null;
                e.printStackTrace();
            }
        }
    }

    protected void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.close();
            progressDialog = null;
        }
    }
}
