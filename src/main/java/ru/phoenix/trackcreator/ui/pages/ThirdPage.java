package ru.phoenix.trackcreator.ui.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import ru.phoenix.trackcreator.entity.Location;
import ru.phoenix.trackcreator.entity.Point;
import ru.phoenix.trackcreator.entity.SurveyData;
import ru.phoenix.trackcreator.helpers.TrackCreatorHelper;
import ru.phoenix.trackcreator.helpers.TrackHelper;
import ru.phoenix.trackcreator.ui.WizardPage;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class ThirdPage extends WizardPage {

    @FXML private TableView<Point> pointsTable;
    @FXML private TableColumn<Point, Date> time;
    @FXML private TableColumn<Point, Double> lat;
    @FXML private TableColumn<Point, Double> lng;
    @FXML private TableColumn<Point, String> name;
    @FXML private TableColumn<Point, String> offset;

    @FXML private TextField saveDir;
    @FXML private Button choiseDir;
    @FXML private Button save;

    private ObservableList<Point> data = FXCollections.observableArrayList();

    public ThirdPage() {
        super("Имена маршрутных точек");
    }

    public Parent getContent() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/third_page.fxml"));
        loader.setController(this);
        return loader.load();
    }

    @Override
    public void onForegraund() {
        fillData();
        pointsTable.setItems(data);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML private void initialize() {
        time.setCellValueFactory(new PropertyValueFactory<Point, Date>("time"));
        lat.setCellValueFactory(new PropertyValueFactory<Point, Double>("lat"));
        lng.setCellValueFactory(new PropertyValueFactory<Point, Double>("lng"));

        name.setCellValueFactory(new PropertyValueFactory<Point, String>("name"));
        name.setCellFactory(TextFieldTableCell.<Point>forTableColumn());
        name.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Point, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Point, String> event) {
                Point point = event.getTableView().getItems().get(event.getTablePosition().getRow());
                point.setName(event.getNewValue());
            }
        });

        offset.setCellValueFactory(new PropertyValueFactory<Point, String>("offset"));
        offset.setCellFactory(TextFieldTableCell.<Point>forTableColumn());
        offset.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Point, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Point, String> event) {
                Point point = event.getTableView().getItems().get(event.getTablePosition().getRow());
                point.setOffset(event.getNewValue());
            }
        });

        choiseDir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                final DirectoryChooser directoryChooser = new DirectoryChooser();
                final File selectedDirectory = directoryChooser.showDialog(getWizard().getStage());
                if (selectedDirectory != null) {
                    saveDir.setText(selectedDirectory.getAbsolutePath());
                    save.setDisable(false);
                }
            }
        });

        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String title = "Создания файла трека";
                try {
                    TrackCreatorHelper.saveTrack(
                            TrackHelper.getTrackWithPause(data),
                            SurveyData.instance.getTrackDate2(),
                            saveDir.getText()
                    );

                    TrackCreatorHelper.saveTrack(
                            SurveyData.instance.getTrackPoints1(),
                            SurveyData.instance.getTrackDate1(),
                            saveDir.getText()
                    );
                    viewAlertDialog(title, "Файл успешно создан и сохранен");
                } catch (Exception ex) {
                    viewAlertDialog(title, ex.getMessage());
                }
            }
        });
    }

    private void fillData() {
        data.clear();
        for (Location point : SurveyData.instance.getTrackPoints2()) {
            data.add(new Point(point));
        }
    }
}
