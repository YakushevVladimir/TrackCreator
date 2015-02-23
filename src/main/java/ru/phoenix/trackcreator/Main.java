package ru.phoenix.trackcreator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.phoenix.trackcreator.ui.SurveyWizard;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class Main extends Application {
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(new SurveyWizard(stage), 600, 300));
        stage.setMinHeight(300);
        stage.setMinWidth(600);
        stage.setTitle("TrackCreator 1.2");
        stage.getIcons().add(new
                Image(getClass().getClassLoader().getResourceAsStream("images/trackcreator.png")));
        stage.show();
    }
}

