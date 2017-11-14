package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private int result_code;

    private int mFPS = 30;
    private int mWidth = -1;
    private int mHeight = -1;

    private String mNewTitle = "output";
    private String mPaletteLocation = "palette.png";

    private List<File> mFilesList = new ArrayList<>();

    private TextField fpsTF;
    private TextField widthTF;
    private TextField heightTF;
    private TextField titleTF;

    private ListView<File> filesListView;

    private Controller controller;

    private String converter;

    @Override
    public void start(Stage primaryStage) throws Exception {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            converter = "ffmpeg.exe";
        } else if (OS.contains("mac")) {
            converter = "./ffmpeg";
        } else {
            throw new Error("Your OS is not support!!");
        }

        GridPane grid = FXMLLoader.load(getClass().getResource("main.fxml"));

        controller = Controller.getInstance();

        bindViews();

        primaryStage.setScene(new Scene(grid, 300, 300));
        primaryStage.setTitle("ffmpegConverter");
        initDragEvents(grid);

        primaryStage.show();
    }

    private void bindViews() {
        fpsTF = controller.getFPS();
        widthTF = controller.getWidth();
        heightTF = controller.getHeight();
        titleTF = controller.getTitle();
        filesListView = controller.getList();

        Button convert = controller.getSubmitButton();
        convert.setOnAction(e -> {
            convertFiles();
        });
    }

    private void initDragEvents(GridPane grid) {
        grid.setOnDragOver(event -> {
            if (event.getGestureSource() != grid
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        grid.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                success = true;

                mFilesList = db.getFiles();

                ObservableList<File> items = FXCollections.observableArrayList(mFilesList);
                filesListView.setItems(items);
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void convertFiles() {
        updateVariables();

        for (File file : mFilesList) {
            try {
                mPaletteLocation = file.getAbsolutePath().replace(file.getName(), "palette.png");

                try {
                    ProcessBuilder createPaletteBuilder = new ProcessBuilder();
                    createPaletteBuilder.command(
                            converter,
                            "-i",
                            "" + file.getAbsolutePath() + "",
                            "-y",
                            "-vf",
                            "palettegen",
                            mPaletteLocation);
                    Process createPalette = createPaletteBuilder.start();
                    result_code = createPalette.waitFor();

                    if (result_code != 0) {
                        return;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                if (mFilesList.size() > 1) {
                    mNewTitle = mNewTitle.concat(String.valueOf(mFilesList.indexOf(file)));
                }

                File f = new File(file.getAbsolutePath().replace(file.getName(), mNewTitle + ".gif"));
                if (f.exists() && !f.isDirectory()) {
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss"));
                    mNewTitle = String.format("%s_%s", mNewTitle, time);
                }

                try {
                    ProcessBuilder createPaletteBuilder = new ProcessBuilder();
                    createPaletteBuilder.command(
                            converter,
                            "-n",
                            "-i",
                            "" + file.getAbsolutePath() + "",
                            "-i",
                            mPaletteLocation,
                            "-filter_complex",
                            "fps=" + mFPS + ",scale=" + mWidth + ":" + mHeight + ":flags=lanczos[x];[x][1:v]paletteuse ",
                            file.getAbsolutePath().replace(file.getName(), mNewTitle + ".gif")
                    );
                    Process createGif = createPaletteBuilder.start();

                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(createGif.getErrorStream()));

                    String s;
                    while ((s = stdError.readLine()) != null) {
                        System.out.println(s);
                    }

                    result_code = createGif.waitFor();

                    if (result_code != 0) {
                        return;
                    }

                    mNewTitle = "output";
                    File palette = new File(mPaletteLocation);
                    if (palette.delete()) {
                        System.out.println(palette.getName() + " was deleted!");
                    } else {
                        System.out.println("Delete operation is failed.");
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateVariables() {
        if (!fpsTF.getText().equals(""))
            mFPS = Integer.parseInt(fpsTF.getText());
        else
            mFPS = 30;

        if (!widthTF.getText().equals(""))
            mWidth = Integer.parseInt(widthTF.getText());
        else
            mWidth = -1;

        if (!heightTF.getText().equals(""))
            mHeight = Integer.parseInt(heightTF.getText());
        else mHeight = -1;

        if (!titleTF.getText().equals(""))
            mNewTitle = titleTF.getText();
        else
            mNewTitle = "output";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
