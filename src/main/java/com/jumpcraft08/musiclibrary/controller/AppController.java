package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.SongFile;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;


public class AppController {

    @FXML
    private TableView<SongFile> TableSong;

    @FXML
    public void SelectFolderController() {
        SelectFolder.Select(SelectFolder.SelectReason.POPULATE_TABLE, TableSong);
    }
}
