package ru.phoenix.trackcreator.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class AlertDialog {
    @FXML private ImageView alertIcon;
    @FXML private Label alertTitle;
    @FXML private Label alertMessage;
    @FXML private Button alertButton;

    private Stage stage;
    private String title;
    private String message;

    public AlertDialog(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public void show(Stage owner) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/alert_dialog.fxml"));
        loader.setController(this);
        Pane gridPane = loader.load();

        stage = new Stage();
        stage.setScene(new Scene(gridPane));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML private void initialize() {
        alertTitle.setText(title);
        alertMessage.setText(message);
        alertIcon.setImage(new Image(getClass().getClassLoader().getResourceAsStream("images/ic_alert.png")));
        alertButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.close();
            }
        });
    }
}
