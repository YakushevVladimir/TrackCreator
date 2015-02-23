package ru.phoenix.trackcreator.ui;

import javafx.stage.Stage;
import ru.phoenix.trackcreator.ui.pages.FirstPage;
import ru.phoenix.trackcreator.ui.pages.SecondPage;
import ru.phoenix.trackcreator.ui.pages.ThirdPage;

/**
 * @author Vladimir Yakushev
 * @version 1.0
 */
public class SurveyWizard extends Wizard {

    public SurveyWizard(Stage owner) {
        super(
                new FirstPage(),
                new SecondPage(),
                new ThirdPage()
        );
        this.owner = owner;
    }

    public void finish() {
        owner.close();
    }
}
