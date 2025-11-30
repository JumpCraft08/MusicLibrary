package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.util.LogManager;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;
import java.util.function.IntConsumer;

public class DialogManager {

    public static void showError(String title, String header, String content, Throwable e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();

        if (e != null) {
            LogManager.logError(DialogManager.class, header + " - " + content, e);
        }
    }

    public static void showRatingDialog(String songName, int currentRating, IntConsumer onRatingEntered) {
        TextInputDialog dialog = new TextInputDialog(currentRating >= 0 ? String.valueOf(currentRating) : "0");
        dialog.setTitle("Calificar canción");
        dialog.setHeaderText("Ingrese un rating del 0 al 5 para \"" + songName + "\":");
        dialog.setContentText("Rating:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int rating = Integer.parseInt(input);
                if (rating < 0) rating = 0;
                if (rating > 5) rating = 5;
                onRatingEntered.accept(rating);
            } catch (NumberFormatException e) {
                showError("Entrada inválida", "El valor ingresado no es un número válido.", "Debe ingresar un número entre 0 y 5.", e);
            }
        });
    }
}
