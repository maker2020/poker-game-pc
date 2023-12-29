package client.fxclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FXEntrance extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("game_menu"));
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        /**
         * 有根目录标识(/)则从classpath根目录开始，否则在.class开始，jar包中使用/
         */
        FXMLLoader fxmlLoader = new FXMLLoader(FXEntrance.class.getResource("/"+fxml + ".fxml"));
        return fxmlLoader.load();
    }
}
