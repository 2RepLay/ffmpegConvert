package converter.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.File;

public class MainScreenController {
    private static MainScreenController instance;

    @FXML
    private Text header;
    @FXML
    private TextField mFPS;
    @FXML
    private TextField mWidth;
    @FXML
    private TextField mHeight;
    @FXML
    private TextField mTitle;
    @FXML
    private Button mSubmitButton;
    @FXML
    private ListView<File> mList;

    public MainScreenController() {

    }

    @FXML
    private void initialize() {
        instance = this;
        header.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));

        mFPS.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                mFPS.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        mWidth.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                mWidth.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        mHeight.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                mHeight.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    public static MainScreenController getInstance() {
        return instance;
    }

    public TextField getFPS() {
        return mFPS;
    }

    public TextField getWidth() {
        return mWidth;
    }

    public TextField getHeight() {
        return mHeight;
    }

    public TextField getTitle() {
        return mTitle;
    }

    public Button getSubmitButton() {
        return mSubmitButton;
    }

    public ListView<File> getList() {
        return mList;
    }
}
