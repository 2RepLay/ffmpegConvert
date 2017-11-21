package converter.view;

import converter.Main;
import converter.controller.ListItemController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;

public class FileListCell extends ListCell<File> {
    @Override
    public void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            try {
                GridPane root = FXMLLoader.load(getClass().getResource("../fxml/list_item.fxml"));

                ListItemController listItemController = ListItemController.getInstance();

                Text text = listItemController.getTitle();
                text.setText(item.getName());

                Button button = listItemController.getRemove();
                button.setOnAction(e -> {
                    Main.removeFile(item);
                });

                setGraphic(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
