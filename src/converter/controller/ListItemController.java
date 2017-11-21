package converter.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.text.Text;

public class ListItemController {
    private static ListItemController instance;

    @FXML
    private Text title;

    @FXML
    private Button remove;

    public ListItemController() {

    }

    public Text getTitle() {
        return title;
    }

    public Button getRemove() {
        return remove;
    }

    @FXML
    private void initialize() {
        instance = this;
    }

    public static ListItemController getInstance() {
        return instance;
    }
}
