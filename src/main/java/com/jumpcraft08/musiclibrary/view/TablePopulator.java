package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.SongFile;
import com.jumpcraft08.musiclibrary.util.TableBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

public class TablePopulator {

    public static void populateTable(TableBundle bundle, File folder) {
        if (bundle == null || folder == null || !folder.isDirectory()) return;

        ObservableList<SongFile> songList = FXCollections.observableArrayList();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    songList.add(new SongFile(file.getName(), "Desconocido"));
                }
            }
        }

        bundle.getFileNameColumn().setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFileName())
        );

        bundle.getArtistColumn().setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getArtist())
        );

        bundle.getTable().setItems(songList);
    }
}
