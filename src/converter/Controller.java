package converter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.File;

public class Controller {
    private static Controller instance;

    @FXML private Text header;
    @FXML private TextField mFPS;
    @FXML private TextField mWidth;
    @FXML private TextField mHeight;
    @FXML private TextField mTitle;
    @FXML private Button mSubmitButton;
    @FXML private ListView<File> mList;

    public Controller() {

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

    static Controller getInstance() {
        return instance;
    }

    TextField getFPS() {
        return mFPS;
    }

    TextField getWidth() {
        return mWidth;
    }

    TextField getHeight() {
        return mHeight;
    }

    TextField getTitle() {
        return mTitle;
    }

    Button getSubmitButton() {
        return mSubmitButton;
    }

    ListView<File> getList() {
        return mList;
    }
}
