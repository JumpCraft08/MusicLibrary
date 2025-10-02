package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.SongFile;
import com.jumpcraft08.musiclibrary.util.TableBundle;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

public class AppController {

    @FXML private TableView<SongFile> TableSong;
    @FXML private TableColumn<SongFile, String> FileNameColumn;
    @FXML private TableColumn<SongFile, String> ArtistColumn;

    @FXML
    public void SelectFolderController() {
        SelectFolder.Select(SelectFolder.SelectReason.POPULATE_TABLE, new TableBundle(TableSong, FileNameColumn, ArtistColumn));
    }
}
