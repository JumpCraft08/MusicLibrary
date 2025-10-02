package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.TableBundle;

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

        bundle.fileNameColumn().setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFileName())
        );

        bundle.artistColumn().setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getArtist())
        );

        bundle.table().setItems(songList);
    }
}
