package ru.phoenix.trackcreator.ui.pages;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import ru.phoenix.trackcreator.helpers.TrackHelper;
import ru.phoenix.trackcreator.ui.WizardPage;

import java.io.File;
import java.io.IOException;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class SecondPage extends WizardPage {

    @FXML private RadioButton rbKmlFile;
    @FXML private TextField kmlFilePath;
    @FXML private Button kmlFileChoice;
    @FXML private RadioButton rbGpxList;
    @FXML private TextField gpxList;
    @FXML private Label gpxListDescription;

    public SecondPage() {
        super("Указание точек маршрута");
    }

    public Parent getContent() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/second_page.fxml"));
        loader.setController(this);
        return loader.load();
    }

    @Override
    public void nextPage() {
        viewProgressDialog("Чтение точек маршрута...");
        try {
            if (rbKmlFile.isSelected()) {
                if (!TrackHelper.addLocationFromFile(kmlFilePath.getText())) return;
            } else {
                if (!TrackHelper.addLocationFromString(gpxList.getText())) return;
            }
        } catch (Exception ex) {
            viewAlertDialog("Ошибка получения координат", ex.getMessage());
            return;
        }
        closeProgressDialog();
        super.nextPage();
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        ToggleGroup gpxGroup = new ToggleGroup();
        rbKmlFile.setToggleGroup(gpxGroup);
        rbGpxList.setToggleGroup(gpxGroup);
        gpxGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle oldToggle, Toggle newToggle) {
                boolean isFile = rbKmlFile.isSelected();
                gpxList.setDisable(isFile);
                gpxListDescription.setDisable(isFile);
                kmlFileChoice.setDisable(!isFile);
                kmlFilePath.setDisable(!isFile);
            }
        });

        kmlFileChoice.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Выберите KMZ-файл с маршрутом");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("KMZ-файл", "*.kmz"));
                File file = fileChooser.showOpenDialog(getWizard().getScene().getWindow());
                if (file != null) {
                    kmlFilePath.setText(file.getAbsolutePath());
                    setNextButtonState();
                }
            }
        });

        gpxList.addEventFilter(KeyEvent.KEY_TYPED, new KeyTypedEvent());
        //gpxList.setText("56.166350,59.494067,A21-12;56.194667,59.498300,C21-12;56.219950,59.476667,B21-12");
    }

    private void setNextButtonState() {
        TextField field = rbKmlFile.isSelected() ? kmlFilePath : gpxList;
        if ("".equals(field.getText())) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
    }

    private class KeyTypedEvent implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            setNextButtonState();
        }
    }
}
