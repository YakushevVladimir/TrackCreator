package ru.phoenix.trackcreator.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class ProgressDialog {
    @FXML private Label dialogMessage;

    private String message;
    private Stage stage;

    public ProgressDialog(String message) throws IOException {
        this.message = message;
        init();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void show(Stage owner) {
        stage.initOwner(owner);
        stage.show();
    }

    public void close() {
        stage.close();
    }

    private void init() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/progress_dialog.fxml"));
        loader.setController(this);
        Pane pane = loader.load();

        stage = new Stage();
        stage.setScene(new Scene(pane));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML private void initialize() {
        dialogMessage.setText(message);
    }
}
