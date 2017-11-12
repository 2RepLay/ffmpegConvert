package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private String converter;

    @Override
    public void start(Stage primaryStage) throws Exception{
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            converter =  "ffmpeg.exe";
        } else if (OS.contains("mac")) {
            converter = "./ffmpeg";
        } else {
            throw new Error("Your OS is not support!!");
        }

        GridPane grid = initUI();

        primaryStage.setScene(new Scene(grid, 400, 300));
        primaryStage.setTitle("ffmpegConverter");
        initDragEvents(grid);

        primaryStage.show();
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

                ObservableList<File> items = FXCollections.observableArrayList (mFilesList);
                filesListView.setItems(items);
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private GridPane initUI() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Drag & Drop .mp4 files here");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label fps = new Label("FPS:");
        grid.add(fps, 0, 1);

        fpsTF = new TextField();
        fpsTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                fpsTF.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        grid.add(fpsTF, 1, 1);

        Label width = new Label("Width:");
        grid.add(width, 0, 2);

        widthTF = new TextField();
        widthTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                widthTF.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        grid.add(widthTF, 1, 2);

        Label height = new Label("Height:");
        grid.add(height, 0, 3);

        heightTF = new TextField();
        heightTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                heightTF.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        grid.add(heightTF, 1, 3);

        Label title = new Label("Title:");
        grid.add(title, 0, 4);

        titleTF = new TextField();
        grid.add(titleTF, 1, 4);

        filesListView = new ListView<>();
        grid.add(filesListView, 0, 5, 2, 2);

        Button convert = new Button("Convert");
        convert.setOnAction(e -> {
            convertFiles();
        });
        grid.add(convert, 0, 7);

        return grid;
    }

    private void convertFiles() {
        if (!fpsTF.getText().equals(""))
            mFPS = Integer.parseInt(fpsTF.getText());

        if (!widthTF.getText().equals(""))
            mWidth = Integer.parseInt(widthTF.getText());

        if (!heightTF.getText().equals(""))
            mHeight = Integer.parseInt(heightTF.getText());

        if (!titleTF.getText().equals(""))
            mNewTitle = titleTF.getText();

        for (File file: mFilesList) {
            try {
                Runtime runtime = Runtime.getRuntime();

                mPaletteLocation = file.getAbsolutePath().replace(file.getName(), "palette.png");

                /* увы, так не работает */
                try{
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

                /* никогда так не делай */
                // Process createPalette = Runtime.getRuntime().exec("ffmpeg.exe -y -i " + file.getAbsolutePath() + " -vf fps=" + mFPS + ",scale=" + mWidth + ":" + mHeight + ":flags=lanczos,palettegen "  + mPaletteLocation);

                if (mFilesList.size() > 1) {
                    mNewTitle = mNewTitle.concat(String.valueOf(mFilesList.indexOf(file)));
                }

                File f = new File(file.getAbsolutePath().replace(file.getName(), mNewTitle + ".gif"));
                if(f.exists() && !f.isDirectory()) {
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss"));
                    mNewTitle = String.format("%s_%s", mNewTitle, time);
                }

                /* увы, и так тоже не работает */
                try{
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

                    /* а так уж и подавно не делай */
                    // Process createGif = Runtime.getRuntime().exec("ffmpeg.exe -i " + file.getAbsolutePath() + " -i " + mPaletteLocation + " -filter_complex fps=" + mFPS + ",scale=" + mWidth + ":" + mHeight + ":flags=lanczos[x];[x][1:v]paletteuse " + file.getAbsolutePath().replace(file.getName(), mNewTitle + ".gif"));
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
                    if(palette.delete()) {
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

    public static void main(String[] args) {
        launch(args);
    }
}
