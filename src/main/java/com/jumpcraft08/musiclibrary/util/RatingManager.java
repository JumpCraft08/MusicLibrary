package com.jumpcraft08.musiclibrary.util;

import com.jumpcraft08.musiclibrary.model.SongFile;
import javafx.scene.control.TableColumn;

import java.util.List;

public class RatingManager {

    public RatingManager() { }

    /** Obtiene el rating de una canción por nombre leyendo directamente del JSON */
    public int getRating(String songName) {
        List<SongFile> cached = JsonCache.load();
        if (cached == null) return -1;
        for (SongFile song : cached) {
            if (song.getFileName().equals(songName)) {
                return song.getRating();
            }
        }
        return -1;
    }

    /** Establece el rating de una canción y actualiza el JSON */
    public void setRating(SongFile song, int rating) {
        song.setRating(rating);  // actualizar objeto en memoria
        JsonCache.updateSongRating(song.getFileName(), rating); // actualizar JSON directamente
    }

    /** Configura la columna de ratings en la tabla */
    public void configureRatingColumn(TableColumn<SongFile, Number> ratingColumn) {
        ratingColumn.setCellValueFactory(cell -> cell.getValue().ratingProperty());
        ratingColumn.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Number rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null || rating.intValue() < 0) {
                    setText("");
                } else {
                    setText(String.valueOf(rating.intValue()));
                }
            }
        });
    }
}
