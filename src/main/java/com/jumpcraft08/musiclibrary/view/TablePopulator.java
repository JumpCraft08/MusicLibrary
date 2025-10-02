package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.SongFile;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

public class TablePopulator {

    public static void populateTable(TableView<SongFile> table, File folder) {
        if (table == null || folder == null || !folder.isDirectory()) return;

        ObservableList<SongFile> songList = FXCollections.observableArrayList();
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                songList.add(new SongFile(file.getName()));
            }
        }

        if (!table.getColumns().isEmpty()) {
            TableColumn<SongFile, String> fileNameColumn = (TableColumn<SongFile, String>) table.getColumns().get(0);
            fileNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFileName())
            );
        }

        table.setItems(songList);
    }
}