package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.ContextMenu;

public class SongContextMenu {
    public static ContextMenu createContextMenu(TableRow<SongFile> row) {
        MenuItem detailsItem = new MenuItem("Detalles");
        detailsItem.setOnAction(e -> {
            if (!row.isEmpty()) {
                SongDetails.showSongDetails(row.getItem());
            }
        });

        return new ContextMenu(detailsItem);
    }
}
