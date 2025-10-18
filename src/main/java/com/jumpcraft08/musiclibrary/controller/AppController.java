package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.TableBundle;
import com.jumpcraft08.musiclibrary.util.ConfigManager;
import com.jumpcraft08.musiclibrary.model.TypeFile;
import com.jumpcraft08.musiclibrary.view.SongContextMenu;
import com.jumpcraft08.musiclibrary.util.OpenSongFile;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.ContextMenu;

import java.io.File;

public class AppController {

    @FXML private TableView<SongFile> TableSong;
    @FXML private TableColumn<SongFile, String> FileNameColumn;
    @FXML private TableColumn<SongFile, String> ArtistColumn;
    @FXML private TableColumn<SongFile, String> VersionsColumn;

    @FXML private MenuItem ArtistColumnMenuItem;
    @FXML private MenuItem VersionsColumnMenuItem;

    private final ConfigManager config = new ConfigManager();

    @FXML
    public void initialize() {
        boolean showArtistColumn = config.getBoolean("showArtistColumn", true);
        ArtistColumn.setVisible(showArtistColumn);
        ArtistColumnMenuItem.setText(showArtistColumn ? "Ocultar Columna Artistas" : "Ver Columna Artistas");

        boolean showVersionsColumn = config.getBoolean("showVersionsColumn", true);
        VersionsColumn.setVisible(showVersionsColumn);
        VersionsColumnMenuItem.setText(showVersionsColumn ? "Ocultar Columna Versiones" : "Ver Columna Versiones");

        TableSong.setRowFactory(tv -> {
            TableRow<SongFile> row = new TableRow<>();

            // Doble clic
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    OpenSongFile.openSong(row.getItem());
                }
            });

            // Menú contextual
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(SongContextMenu.createContextMenu(row))
            );

            return row;
        });
    }

    @FXML
    public void SelectFolderController() {
        SelectFolder.Select(SelectFolder.SelectReason.POPULATE_TABLE, new TableBundle(TableSong, FileNameColumn, ArtistColumn, VersionsColumn), TypeFile.FLAC_HI_RES, TypeFile.FLAC_CD, TypeFile.M4A);
    }

    @FXML
    public void HideArtistColumn() {
        toggleColumnVisibility(ArtistColumn, ArtistColumnMenuItem, "showArtistColumn",
                "Ver Columna Artistas", "Ocultar Columna Artistas");
    }

    @FXML
    public void HideVersionsColumn() {
        toggleColumnVisibility(VersionsColumn, VersionsColumnMenuItem, "showVersionsColumn",
                "Ver Columna Versiones", "Ocultar Columna Versiones");
    }

    private void toggleColumnVisibility(TableColumn<?, ?> column, MenuItem menuItem, String configKey, String showText, String hideText) {
        boolean currentlyVisible = column.isVisible();
        boolean newState = !currentlyVisible;

        column.setVisible(newState);
        menuItem.setText(newState ? hideText : showText);

        config.setBoolean(configKey, newState);
    }

}
