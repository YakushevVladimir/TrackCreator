package ru.phoenix.trackcreator.ui.pages;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.phoenix.trackcreator.entity.SurveyData;
import ru.phoenix.trackcreator.ui.WizardPage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class FirstPage extends WizardPage {

    private static final int MAX_SPEED_VALUE = 50;
    private static final int MAX_CAD_VALUE = 254;

    @FXML private TextField trackName;
    @FXML private TextField trackDevice;
    @FXML private TextField trackSpeed;
    @FXML private TextField trackCad;
    @FXML private TextField trackHour1;
    @FXML private TextField trackMinute1;
    @FXML private TextField trackSecond1;
    @FXML private DatePicker trackDate1;
    @FXML private TextField trackHour2;
    @FXML private TextField trackMinute2;
    @FXML private TextField trackSecond2;
    @FXML private DatePicker trackDate2;

    public FirstPage() {
        super("Ввод параметров маршрута");
        nextButton.setDisable(true);
    }

    public Parent getContent() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/first_page.fxml"));
        loader.setController(this);
        return loader.load();
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML private void initialize() {
        ChangeListener<String> listener = new TextChangeListener();
        trackName.textProperty().addListener(listener);
        trackDevice.textProperty().addListener(listener);
        trackSpeed.textProperty().addListener(listener);
        trackCad.textProperty().addListener(listener);

        trackHour1.textProperty().addListener(listener);
        trackMinute1.textProperty().addListener(listener);
        trackSecond1.textProperty().addListener(listener);
        trackDate1.getEditor().textProperty().addListener(listener);

        trackHour2.textProperty().addListener(listener);
        trackMinute2.textProperty().addListener(listener);
        trackSecond2.textProperty().addListener(listener);
        trackDate2.getEditor().textProperty().addListener(listener);

        trackSpeed.addEventFilter(KeyEvent.KEY_TYPED, new DecimalEventHandler(MAX_SPEED_VALUE, 2));
        trackCad.addEventFilter(KeyEvent.KEY_TYPED, new DecimalEventHandler(MAX_CAD_VALUE, 3));

        trackHour1.addEventFilter(KeyEvent.KEY_TYPED, new DecimalEventHandler(23, 2));
        trackMinute1.addEventFilter(KeyEvent.KEY_TYPED, new DecimalEventHandler(59, 2));
        trackSecond1.addEventFilter(KeyEvent.KEY_TYPED, new DecimalEventHandler(59, 2));

        trackHour2.addEventFilter(KeyEvent.KEY_TYPED, new DecimalEventHandler(23, 2));
        trackMinute2.addEventFilter(KeyEvent.KEY_TYPED, new DecimalEventHandler(59, 2));
        trackSecond2.addEventFilter(KeyEvent.KEY_TYPED, new DecimalEventHandler(59, 2));

        Calendar cal = new GregorianCalendar();
        cal.setTime(SurveyData.instance.getTrackDate1());

        trackDate1.setValue(LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
        trackHour1.setText(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
        trackMinute1.setText(String.format("%02d", cal.get(Calendar.MINUTE)));
        trackSecond1.setText(String.format("%02d", cal.get(Calendar.SECOND)));

        trackDate2.setValue(LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
        trackHour2.setText(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
        trackMinute2.setText(String.format("%02d", cal.get(Calendar.MINUTE)));
        trackSecond2.setText(String.format("%02d", cal.get(Calendar.SECOND)));

        trackDevice.setText(String.valueOf(SurveyData.instance.getTrackDevice()));
        trackCad.setText(String.valueOf(SurveyData.instance.getTrackCad()));
        trackSpeed.setText(String.valueOf(SurveyData.instance.getTrackSpeed()));
    }

    @Override
    public void nextPage() {
        try {
            setSurveyData();
            super.nextPage();
        } catch (IllegalArgumentException ex) {
            viewAlertDialog("Ошибка", "Указаны неверные параматеры!");
        }
    }

    private void setSurveyData() throws IllegalArgumentException {
        int speed = Integer.parseInt(trackSpeed.getText().trim());
        int cad = Integer.parseInt(trackCad.getText().trim());
        int hour1 = Integer.parseInt(trackHour1.getText());
        int minute1 = Integer.parseInt(trackMinute1.getText());
        int second1 = Integer.parseInt(trackSecond1.getText());
        int hour2 = Integer.parseInt(trackHour2.getText());
        int minute2 = Integer.parseInt(trackMinute2.getText());
        int second2 = Integer.parseInt(trackSecond2.getText());

        if (speed > MAX_SPEED_VALUE || cad > MAX_CAD_VALUE
                || hour1 > 23 || minute1 > 59 || second1 > 59
                || hour2 > 23 || minute2 > 59 || second2 > 59) {
            throw new IllegalArgumentException();
        }

        SurveyData.instance.setTrackName(trackName.getText().trim());
        SurveyData.instance.setTrackDevice(trackDevice.getText().trim());
        SurveyData.instance.setTrackSpeed(speed);
        SurveyData.instance.setTrackCad(cad);

        LocalDate localDate1 = trackDate1.getValue();
        SurveyData.instance.setTrackDate1(new GregorianCalendar(
                localDate1.getYear(), localDate1.getMonthValue() - 1, localDate1.getDayOfMonth(),
                hour1, minute1, second1)
                .getTime());

        LocalDate localDate2 = trackDate2.getValue();
        SurveyData.instance.setTrackDate2(new GregorianCalendar(
                localDate2.getYear(), localDate2.getMonthValue() - 1, localDate2.getDayOfMonth(),
                hour2, minute2, second2)
                .getTime());
    }

    private void setNextButtonState() {
        if ("".equals(trackName.getText())
                || "".equals(trackDevice.getText())
                || "".equals(trackDate1.getEditor().getText())
                || "".equals(trackHour1.getText())
                || "".equals(trackMinute1.getText())
                || "".equals(trackSecond1.getText())
                || "".equals(trackDate2.getEditor().getText())
                || "".equals(trackHour2.getText())
                || "".equals(trackMinute2.getText())
                || "".equals(trackSecond2.getText())
                || "".equals(trackSpeed.getText())
                || "".equals(trackCad.getText())) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
    }

    private class DecimalEventHandler implements EventHandler<KeyEvent> {
        private int maxLength;
        private int maxValue;

        public DecimalEventHandler(int maxValue, int maxLength) {
            this.maxLength = maxLength;
            this.maxValue = maxValue;
        }

        @Override
        public void handle(KeyEvent event) {
            if (event.getCode().equals(KeyCode.BACK_SPACE)) {
                return;
            }

            if (!"0123456789".contains(event.getCharacter())) {
                event.consume();
            } else{
                TextField field = (TextField) event.getSource();
                IndexRange range = field.getSelection();
                int length = field.getLength() - range.getLength() + event.getCharacter().length();
                if (length == 0) {
                    return;
                } else if (length > maxLength) {
                    event.consume();
                    return;
                }

                String text = field.getText();
                StringBuilder builder = new StringBuilder();
                builder.append(text.substring(0, range.getStart()));
                builder.append(event.getCharacter());
                if (range.getEnd() < text.length()) {
                    builder.append(text.substring(range.getEnd()));
                }

                if (Integer.parseInt(builder.toString()) > maxValue) {
                    field.setStyle("-fx-text-inner-color: red;");
                } else {
                    field.setStyle("-fx-text-inner-color: black;");
                }
            }
        }
    }

    private class TextChangeListener implements ChangeListener<String> {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            setNextButtonState();
        }
    }
}
