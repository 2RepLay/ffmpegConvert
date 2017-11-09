package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        root.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                System.out.println(event);

                if (event.getGestureSource() != root
                        && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });

        root.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                System.out.println("DROP");
                System.out.println(event);
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasFiles()) {
                    success = true;

                    List<File> files = db.getFiles();

                    for (File file: files) {
                        System.out.println(file.getName());
                        System.out.println(file.getAbsolutePath());

                        try {

                            Process process1 = Runtime.getRuntime().exec("./ffmpeg -y -i " + file.getAbsolutePath() + " -vf fps=25,scale=-1:-1:flags=lanczos,palettegen "  + file.getAbsolutePath().replace(file.getName(), "palette.png"));
                            int i = process1.waitFor();

                            System.out.println("END PROCESS " + i);


                            Process process2 = Runtime.getRuntime().exec("./ffmpeg -i " + file.getAbsolutePath() + " -i " + file.getAbsolutePath().replace(file.getName(), "palette.png") + " -filter_complex fps=30,scale=-1:-1:flags=lanczos[x];[x][1:v]paletteuse " + file.getAbsolutePath().replace(file.getName(), "output.gif"));
                            int j = process2.waitFor();

                            System.out.println("END PROCESS " + j);
                            System.out.println(file.getAbsolutePath().replace(file.getName(), "output.gif"));
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                /* let the source know whether the string was successfully
                 * transferred and used */
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
