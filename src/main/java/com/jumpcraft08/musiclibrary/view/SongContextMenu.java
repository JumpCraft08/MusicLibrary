package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.util.RatingManager;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.ContextMenu;

public class SongContextMenu {
    private static final RatingManager ratingManager = new RatingManager();

    public static ContextMenu createContextMenu(TableRow<SongFile> row) {
        MenuItem detailsItem = new MenuItem("Detalles");
        detailsItem.setOnAction(e -> {
            if (!row.isEmpty()) {
                SongDetails.showSongDetails(row.getItem());
            }
        });

        MenuItem ratingsItem = new MenuItem("Calificar");
        ratingsItem.setOnAction(e -> {
            if (!row.isEmpty()) {
                SongFile song = row.getItem();
                DialogManager.showRatingDialog(song.getFileName(), song.getRating(), rating -> {
                    song.setRating(rating);
                    ratingManager.setRating(song.getFileName(), rating);
                    row.getTableView().refresh();
                });
            }
        });

        return new ContextMenu(detailsItem, ratingsItem);
    }
}
