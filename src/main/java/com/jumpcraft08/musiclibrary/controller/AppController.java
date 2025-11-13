package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.TableBundle;
import com.jumpcraft08.musiclibrary.util.ConfigManager;
import com.jumpcraft08.musiclibrary.model.TypeFile;
import com.jumpcraft08.musiclibrary.view.SongContextMenu;
import com.jumpcraft08.musiclibrary.util.OpenSongFile;
import com.jumpcraft08.musiclibrary.util.RatingManager;
import com.jumpcraft08.musiclibrary.util.FlacPlayer;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

import java.io.File;

public class AppController {

    @FXML protected TableView<SongFile> TableSong;
    @FXML protected TableColumn<SongFile, File> CoverColumn;
    @FXML protected TableColumn<SongFile, String> FileNameColumn;
    @FXML protected TableColumn<SongFile, String> ArtistColumn;
    @FXML protected TableColumn<SongFile, String> VersionsColumn;
    @FXML protected TableColumn<SongFile, Number> RatingColumn;

    @FXML private MenuItem ArtistColumnMenuItem;
    @FXML private MenuItem VersionsColumnMenuItem;
    @FXML private MenuItem CoverColumnMenuItem;
    @FXML private MenuItem RatingColumnMenuItem;

    @FXML private Button PlayPauseButton;
    @FXML private Slider PlaybackSlider;

    private final FlacPlayer flacPlayer = new FlacPlayer();

    private final Thread[] sliderThreadHolder = new Thread[1];
    private final boolean[] stopSliderUpdaterWrapper = new boolean[1];
    private final boolean[] userDraggingSliderWrapper = new boolean[1];

    private final ConfigManager config = new ConfigManager();
    private final RatingManager ratingManager = new RatingManager();

    @FXML
    public void initialize() {
        setupColumn(ArtistColumn, ArtistColumnMenuItem, "showArtistColumn", "Ver Columna Artistas", "Ocultar Columna Artistas");
        setupColumn(VersionsColumn, VersionsColumnMenuItem, "showVersionsColumn", "Ver Columna Versiones", "Ocultar Columna Versiones");
        setupColumn(RatingColumn, RatingColumnMenuItem, "showRatingColumn", "Ver Columna Rating", "Ocultar Columna Rating");
        setupColumn(CoverColumn, CoverColumnMenuItem, "showCoverColumn", "Ver Columna Cover", "Ocultar Covers");

        ratingManager.configureRatingColumn(RatingColumn);
        CoverColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCoverFile()));
        CoverColumn.setCellFactory(com.jumpcraft08.musiclibrary.view.RenderCover.create());

        TableSong.setRowFactory(tv -> {
            TableRow<SongFile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    OpenSongFile.openSong(
                            row.getItem(), flacPlayer, PlaybackSlider, PlayPauseButton,
                            sliderThreadHolder, stopSliderUpdaterWrapper, userDraggingSliderWrapper
                    );
                }
            });
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(SongContextMenu.createContextMenu(row))
            );
            row.prefHeightProperty().bind(
                    javafx.beans.binding.Bindings.when(CoverColumn.visibleProperty())
                            .then(64.0)
                            .otherwise(24.0)
            );
            return row;
        });

        PlayPauseButton.setOnAction(e -> {
            if (flacPlayer.isPlaying()) {
                flacPlayer.pause();
                PlayPauseButton.setText("Play");
            } else {
                flacPlayer.play();
                PlayPauseButton.setText("Pause");
            }
        });

        PlaybackSlider.setOnMousePressed(e -> userDraggingSliderWrapper[0] = true);
        PlaybackSlider.setOnMouseReleased(e -> {
            userDraggingSliderWrapper[0] = false;
            if (flacPlayer.getTotalSamples() > 0)
                flacPlayer.seek((long) PlaybackSlider.getValue());
        });
    }

    private void setupColumn(TableColumn<?, ?> column, MenuItem menuItem, String configKey, String showText, String hideText) {
        boolean visible = config.getBoolean(configKey, true);
        column.setVisible(visible);
        menuItem.setText(visible ? hideText : showText);
    }

    @FXML
    public void SelectFolderController() {
        SelectFolder.Select(SelectFolder.SelectReason.POPULATE_TABLE, new TableBundle(TableSong, FileNameColumn, ArtistColumn, VersionsColumn, RatingColumn), TypeFile.FLAC_HI_RES, TypeFile.FLAC_CD, TypeFile.M4A);
    }

    @FXML public void HideArtistColumn() {toggleColumnVisibility(ArtistColumn, ArtistColumnMenuItem, "showArtistColumn", "Ver Columna Artistas", "Ocultar Columna Artistas");}
    @FXML public void HideVersionsColumn() {toggleColumnVisibility(VersionsColumn, VersionsColumnMenuItem, "showVersionsColumn", "Ver Columna Versiones", "Ocultar Columna Versiones");}
    @FXML public void HideCoverColumn() {toggleColumnVisibility(CoverColumn, CoverColumnMenuItem, "showCoverColumn", "Ver Columna Cover", "Ocultar Columna Cover");}
    @FXML public void HideRatingColumn() {toggleColumnVisibility(RatingColumn, RatingColumnMenuItem, "showRatingColumn", "Ver Columna Rating", "Ocultar Columna Rating");}

    private void toggleColumnVisibility(TableColumn<?, ?> column, MenuItem menuItem, String configKey, String showText, String hideText) {
        boolean currentlyVisible = column.isVisible();
        boolean newState = !currentlyVisible;

        column.setVisible(newState);
        menuItem.setText(newState ? hideText : showText);

        config.setBoolean(configKey, newState);
    }

    public void shutdown() {
        try {
            // Detener la reproducción
            if (flacPlayer != null) {
                flacPlayer.stop();
            }

            // Detener el hilo del slider
            stopSliderUpdaterWrapper[0] = true;

            if (sliderThreadHolder[0] != null && sliderThreadHolder[0].isAlive()) {
                sliderThreadHolder[0].interrupt();
            }

            System.out.println("Reproductor detenido correctamente.");
        } catch (Exception e) {
            System.err.println("Error al cerrar el reproductor: " + e.getMessage());
        }
    }


}
