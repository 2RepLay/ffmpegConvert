package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Controller {
    private static Controller instance;

    @FXML private TextField mFPS;
    @FXML private TextField mWidth;
    @FXML private TextField mHeight;
    @FXML private TextField mTitle;

    public Controller() {

    }

    @FXML
    private void initialize() {
        instance = this;
    }

    static Controller getInstance() {
        return instance;
    }

    public void setFPS(String text) {
        mFPS.setText(text);
    }
}
