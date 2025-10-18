package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.TableBundle;
import com.jumpcraft08.musiclibrary.util.ConfigManager;
import com.jumpcraft08.musiclibrary.model.TypeFile;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

public class AppController {

    @FXML private TableView<SongFile> TableSong;
    @FXML private TableColumn<SongFile, String> FileNameColumn;
    @FXML private TableColumn<SongFile, String> ArtistColumn;
    @FXML private TableColumn<SongFile, String> VersionsColumn;

    @FXML private MenuItem ArtistColumnMenuItem;

    private final ConfigManager config = new ConfigManager();

    @FXML
    public void initialize() {
        boolean showArtistColumn = config.getBoolean("showArtistColumn", true);
        ArtistColumn.setVisible(showArtistColumn);
        updateMenuItemText(showArtistColumn);
    }

    @FXML
    public void SelectFolderController() {
        SelectFolder.Select(SelectFolder.SelectReason.POPULATE_TABLE, new TableBundle(TableSong, FileNameColumn, ArtistColumn, VersionsColumn), TypeFile.FLAC_HI_RES, TypeFile.FLAC_CD, TypeFile.M4A);
    }

    @FXML
    public void HideArtistColumn() {
        boolean currentlyVisible = ArtistColumn.isVisible();
        boolean newState = !currentlyVisible;

        ArtistColumn.setVisible(newState);
        updateMenuItemText(newState);

        // Guardar en config
        config.setBoolean("showArtistColumn", newState);
    }

    private void updateMenuItemText(boolean visible) {
        if (visible) {
            ArtistColumnMenuItem.setText("Ocultar Columna Artistas");
        } else {
            ArtistColumnMenuItem.setText("Ver Columna Artistas");
        }
    }
}
