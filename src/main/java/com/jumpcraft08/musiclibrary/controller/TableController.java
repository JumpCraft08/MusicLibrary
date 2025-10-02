package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.SongFile;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TableController {

    @FXML
    private TableView<SongFile> TableSong;

    @FXML
    private TableColumn<SongFile, String> FileNameColumn;

    protected TableView<SongFile> getTableSong() {
        return TableSong;
    }

}
