package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.util.RatingManager;
import com.jumpcraft08.musiclibrary.model.Playlist;
import com.jumpcraft08.musiclibrary.util.PlaylistManager;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.Menu;
import javafx.scene.control.ContextMenu;

public class SongContextMenu {

    private static final RatingManager ratingManager = new RatingManager();
    private static final PlaylistManager playlistManager = new PlaylistManager();


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
                    ratingManager.setRating(song, rating); // actualiza el JSON correctamente
                    row.getTableView().refresh();
                });
            }
        });

        // Menu para añadir a playlists
        Menu addToPlaylistMenu = new Menu("Añadir a Playlist");
        for (Playlist pl : playlistManager.getPlaylists()) {
            MenuItem plItem = new MenuItem(pl.getName());
            plItem.setOnAction(ev -> {
                if (!row.isEmpty()) {
                    playlistManager.addSongToPlaylist(row.getItem(), pl);
                    playlistManager.savePlaylists(); // <-- guardar en JSON
                }
            });
            addToPlaylistMenu.getItems().add(plItem);
        }


        return new ContextMenu(detailsItem, ratingsItem, addToPlaylistMenu);
    }
}
