package converter;

import converter.controller.MainScreenController;
import converter.view.FileListCell;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private static Main context;

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

    private MainScreenController controller;

    private String converter;

    public static Main getContext() {
        return context;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        context = this;

        prepareFFmpeg();

        GridPane grid = FXMLLoader.load(getClass().getResource("fxml/main.fxml"));

        controller = MainScreenController.getInstance();

        bindViews();

        primaryStage.setScene(new Scene(grid, 300, 300));
        primaryStage.setTitle("ffmpegConverter");
        initDragEvents(grid);

        primaryStage.show();
    }

    private void prepareFFmpeg() throws IOException {
        String OS = System.getProperty("os.name").toLowerCase();

        if (OS.contains("win")) {
            converter = "ffmpeg.exe";
        } else if (OS.contains("mac")) {
            converter = "ffmpeg";
        } else {
            throw new Error("Your OS is not support!!");
        }

        converter = findExecutable(converter);

        if (OS.contains("mac")) {
            Runtime.getRuntime().exec("chmod +x " + converter);
        }
    }

    private String findExecutable(String filename) throws IOException {
        File converterDir = new File(System.getProperty("user.home") + "/ffmpegConverter");
             converterDir.mkdir();
             converterDir.deleteOnExit();

        File file = new File(converterDir + "/" + filename);
        file.deleteOnExit();

        InputStream is = getClass().getResource("resources/" + filename).openStream();
        OutputStream os = new FileOutputStream(file);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();

        return file.getAbsolutePath();
    }

    private void bindViews() {
        fpsTF = controller.getFPS();
        widthTF = controller.getWidth();
        heightTF = controller.getHeight();
        titleTF = controller.getTitle();
        filesListView = controller.getList();
        filesListView.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override
            public ListCell<File> call(ListView<File> param) {
                return new FileListCell();
            }
        });

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

    public static void removeFile(File file) {
        System.out.println("Remove: " + file.getName() + ", size: " + getContext().mFilesList.size());

        getContext().mFilesList.remove(file);

        ObservableList<File> items = FXCollections.observableArrayList(getContext().mFilesList);
        getContext().filesListView.setItems(items);

        System.out.println("New size: " + getContext().mFilesList.size());
    }
}
