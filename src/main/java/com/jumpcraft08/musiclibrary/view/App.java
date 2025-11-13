package com.jumpcraft08.musiclibrary.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.stage.Stage;

import com.jumpcraft08.musiclibrary.controller.AppController;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/jumpcraft08/musiclibrary/app.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        stage.setTitle("Librería Musical");

        stage.setScene(scene);

        AppController controller = fxmlLoader.getController();

        // Detectar cierre de la ventana
        stage.setOnCloseRequest(event -> {
            controller.shutdown(); // ← esto llama al método que hiciste
            System.out.println("Cerrando aplicación...");
            Platform.exit();
        });


        stage.show();
    }
}
