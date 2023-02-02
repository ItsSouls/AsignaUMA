package es.uma.asignauma;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Vista/Vista.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        stage.getIcons().add(new Image("UMA_logo.png"));
        stage.setTitle("AsignaUMA");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}