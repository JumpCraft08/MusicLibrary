package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.TableBundle;
import com.jumpcraft08.musiclibrary.util.ConfigManager;
import com.jumpcraft08.musiclibrary.model.TypeFile;
import com.jumpcraft08.musiclibrary.view.SongContextMenu;
import com.jumpcraft08.musiclibrary.util.OpenSongFile;
import com.jumpcraft08.musiclibrary.util.RatingManager;
import com.jumpcraft08.musiclibrary.view.FlacPlayer;


import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

import java.io.File;

public class AppController {

    @FXML private TableView<SongFile> TableSong;
    @FXML private TableColumn<SongFile, File> CoverColumn;
    @FXML private TableColumn<SongFile, String> FileNameColumn;
    @FXML private TableColumn<SongFile, String> ArtistColumn;
    @FXML private TableColumn<SongFile, String> VersionsColumn;
    @FXML private TableColumn<SongFile, Number> RatingColumn;

    @FXML private MenuItem ArtistColumnMenuItem;
    @FXML private MenuItem VersionsColumnMenuItem;
    @FXML private MenuItem CoverColumnMenuItem;
    @FXML private MenuItem RatingColumnMenuItem;

    @FXML private HBox PlaybackControls;
    @FXML private Button PlayPauseButton;
    @FXML private Slider PlaybackSlider;

    private FlacPlayer flacPlayer = new FlacPlayer();
    private SongFile currentSong;

    private Thread sliderUpdaterThread; // hilo único para actualizar slider
    private volatile boolean stopSliderUpdater = false;

    private volatile boolean userDraggingSlider = false;

    private Thread[] sliderThreadHolder = new Thread[1];
    private boolean[] stopSliderUpdaterWrapper = new boolean[1]; // wrapper para detener slider
    private boolean[] userDraggingSliderWrapper = new boolean[1];

    private final ConfigManager config = new ConfigManager();
    private final RatingManager ratingManager = new RatingManager();

    @FXML
    public void initialize() {
        boolean showArtistColumn = config.getBoolean("showArtistColumn", true);
        ArtistColumn.setVisible(showArtistColumn);
        ArtistColumnMenuItem.setText(showArtistColumn ? "Ocultar Columna Artistas" : "Ver Columna Artistas");

        boolean showVersionsColumn = config.getBoolean("showVersionsColumn", true);
        VersionsColumn.setVisible(showVersionsColumn);
        VersionsColumnMenuItem.setText(showVersionsColumn ? "Ocultar Columna Versiones" : "Ver Columna Versiones");

        boolean showRatingColumn = config.getBoolean("showRatingColumn", true);
        RatingColumn.setVisible(showRatingColumn);
        RatingColumnMenuItem.setText(showRatingColumn ? "Ocultar Columna Rating" : "Ver Columna Rating");
        ratingManager.configureRatingColumn(RatingColumn);

        boolean showCoverColumn = config.getBoolean("showCoverColumn", false);
        CoverColumn.setVisible(showCoverColumn);
        CoverColumnMenuItem.setText(showCoverColumn ? "Ocultar Covers" : "Ver Covers");
        CoverColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getCoverFile())
        );
        CoverColumn.setCellFactory(com.jumpcraft08.musiclibrary.view.RenderCover.create());


        TableSong.setRowFactory(tv -> {
            TableRow<SongFile> row = new TableRow<>();

            // Doble clic
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    OpenSongFile.openSong(
                            row.getItem(),               // canción
                            flacPlayer,                  // reproductor
                            PlaybackSlider,              // slider
                            PlayPauseButton,             // botón
                            sliderThreadHolder,          // hilo del slider
                            stopSliderUpdaterWrapper,    // wrapper para detener slider
                            userDraggingSliderWrapper    // wrapper para arrastre
                    );
                }
            });

            // Menú contextual
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
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

        // NUEVO: Slider para hacer seek
        PlaybackSlider.setOnMouseReleased(e -> {
            if (flacPlayer.getTotalSamples() > 0) {
                flacPlayer.seek((long) PlaybackSlider.getValue());
            }
        });

        PlaybackSlider.setOnMousePressed(e -> userDraggingSliderWrapper[0] = true);
        PlaybackSlider.setOnMouseReleased(e -> {
            userDraggingSliderWrapper[0] = false;
            if (flacPlayer.getTotalSamples() > 0) {
                flacPlayer.seek((long) PlaybackSlider.getValue());
            }
        });
    }

    private void playSong(SongFile song) {
        try {
            File file = song.getPreferredFile();
            if (file != null && file.exists()) {
                // Detener reproducción anterior y slider
                flacPlayer.stop();
                stopSliderUpdater = true;
                if (sliderUpdaterThread != null && sliderUpdaterThread.isAlive()) {
                    sliderUpdaterThread.join();
                }

                flacPlayer.open(file);
                currentSong = song;

                // Reiniciar slider
                PlaybackSlider.setMin(0);
                PlaybackSlider.setMax(flacPlayer.getTotalSamples());
                PlaybackSlider.setValue(0);

                stopSliderUpdater = false;
                sliderUpdaterThread = new Thread(() -> {
                    while (!stopSliderUpdater && (flacPlayer.isPlaying() || flacPlayer.getCurrentSample() < flacPlayer.getTotalSamples())) {
                        // Solo actualizar si el usuario NO está arrastrando el slider
                        if (!userDraggingSlider) {
                            final double pos = flacPlayer.getCurrentSample();
                            javafx.application.Platform.runLater(() -> PlaybackSlider.setValue(pos));
                        }
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    }
                });
                sliderUpdaterThread.setDaemon(true);
                sliderUpdaterThread.start();

                // Forzar slider a 0 justo antes de iniciar
                javafx.application.Platform.runLater(() -> PlaybackSlider.setValue(0));

                flacPlayer.play();
                PlayPauseButton.setText("Pause");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void SelectFolderController() {
        SelectFolder.Select(SelectFolder.SelectReason.POPULATE_TABLE, new TableBundle(TableSong, FileNameColumn, ArtistColumn, VersionsColumn, RatingColumn), TypeFile.FLAC_HI_RES, TypeFile.FLAC_CD, TypeFile.M4A);
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

    @FXML
    public void HideCoverColumn() {
        toggleColumnVisibility(CoverColumn, CoverColumnMenuItem, "showCoverColumn",
                "Ver Columna Cover", "Ocultar Columna Cover");
    }

    @FXML
    public void HideRatingColumn() {
        toggleColumnVisibility(RatingColumn, RatingColumnMenuItem, "showRatingColumn",
                "Ver Columna Rating", "Ocultar Columna Rating");
    }

    private void toggleColumnVisibility(TableColumn<?, ?> column, MenuItem menuItem, String configKey, String showText, String hideText) {
        boolean currentlyVisible = column.isVisible();
        boolean newState = !currentlyVisible;

        column.setVisible(newState);
        menuItem.setText(newState ? hideText : showText);

        config.setBoolean(configKey, newState);
    }

}
