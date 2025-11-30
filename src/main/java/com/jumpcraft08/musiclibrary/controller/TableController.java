package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.view.SongContextMenu;
import com.jumpcraft08.flac_implementation.utils.OpenSongFile;
import com.jumpcraft08.flac_implementation.player.FlacPlayer;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.*;

public class TableController {

    private final TableView<SongFile> table;
    private final ObjectProperty<SongFile> currentlyPlaying;
    private final Slider playbackSlider;
    private final Button playPauseButton;
    private final Thread[] sliderThreadHolder;
    private final boolean[] stopSliderUpdaterWrapper;
    private final boolean[] userDraggingSliderWrapper;
    private final FlacPlayer flacPlayer;

    public TableController(TableView<SongFile> table,
                           ObjectProperty<SongFile> currentlyPlaying,
                           Slider playbackSlider,
                           Button playPauseButton,
                           FlacPlayer flacPlayer,
                           Thread[] sliderThreadHolder,
                           boolean[] stopSliderUpdaterWrapper,
                           boolean[] userDraggingSliderWrapper) {
        this.table = table;
        this.currentlyPlaying = currentlyPlaying;
        this.playbackSlider = playbackSlider;
        this.playPauseButton = playPauseButton;
        this.flacPlayer = flacPlayer;
        this.sliderThreadHolder = sliderThreadHolder;
        this.stopSliderUpdaterWrapper = stopSliderUpdaterWrapper;
        this.userDraggingSliderWrapper = userDraggingSliderWrapper;
    }

    /** Configura las filas de la tabla con listeners y menú contextual */
    public void setupTableRows(TableColumn<SongFile, ?> coverColumn) {
        table.setRowFactory(tv -> {
            TableRow<SongFile> row = new TableRow<>();
            // Actualiza el estilo según la canción actual
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateRowStyle(row, newItem));
            currentlyPlaying.addListener((obs, oldSong, newSong) -> updateRowStyle(row, row.getItem()));

            // Doble clic para reproducir
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    SongFile song = row.getItem();
                    currentlyPlaying.set(song);
                    OpenSongFile.openSong(song, flacPlayer, playbackSlider, playPauseButton,
                            sliderThreadHolder, stopSliderUpdaterWrapper, userDraggingSliderWrapper);
                }
            });

            // Menú contextual y altura de fila según visibilidad de la columna
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(SongContextMenu.createContextMenu(row))
            );
            row.prefHeightProperty().bind(
                    javafx.beans.binding.Bindings.when(coverColumn.visibleProperty())
                            .then(64.0)
                            .otherwise(24.0)
            );

            return row;
        });
    }

    private void updateRowStyle(TableRow<SongFile> row, SongFile item) {
        row.setStyle(item != null && item.equals(currentlyPlaying.get()) ?
                "-fx-background-color: lightgreen;" : "");
    }
}
