package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.file_manager.controller.SelectFolder;
import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.file_manager.model.TableBundle;
import com.jumpcraft08.musiclibrary.model.TypeFile;
import com.jumpcraft08.file_manager.controller.ConfigManager;
import com.jumpcraft08.flac_implementation.player.FlacPlayer;
import com.jumpcraft08.musiclibrary.util.RatingManager;
import com.jumpcraft08.musiclibrary.view.App;
import com.jumpcraft08.musiclibrary.view.TablePopulator;
import com.jumpcraft08.musiclibrary.model.Playlist;
import com.jumpcraft08.file_manager.util.FileTableLoader;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Map;

public class AppController {

    @FXML protected TableView<SongFile> TableSong;
    @FXML protected TableColumn<SongFile, File> CoverColumn;
    @FXML protected TableColumn<SongFile, String> FileNameColumn;
    @FXML protected TableColumn<SongFile, String> ArtistColumn;
    @FXML protected TableColumn<SongFile, String> AlbumColumn;
    @FXML protected TableColumn<SongFile, String> VersionsColumn;
    @FXML protected TableColumn<SongFile, Number> RatingColumn;

    @FXML private MenuItem ArtistColumnMenuItem;
    @FXML private MenuItem AlbumColumnMenuItem;
    @FXML private MenuItem VersionsColumnMenuItem;
    @FXML private MenuItem CoverColumnMenuItem;
    @FXML private MenuItem RatingColumnMenuItem;

    @FXML private Label NowPlayingLabel;
    @FXML private Button PlayPauseButton;
    @FXML private Slider PlaybackSlider;

    private final ObjectProperty<SongFile> currentlyPlaying = new SimpleObjectProperty<>();
    private final FlacPlayer flacPlayer = new FlacPlayer();

    private final Thread[] sliderThreadHolder = new Thread[1];
    private final boolean[] stopSliderUpdaterWrapper = new boolean[1];
    private final boolean[] userDraggingSliderWrapper = new boolean[1];

    private final ConfigManager config = new ConfigManager();
    private final RatingManager ratingManager = new RatingManager();

    private final ObservableList<SongFile> masterSongList = FXCollections.observableArrayList();
    private FilteredList<SongFile> filteredSongs;

    private MusicPlayerController musicPlayer;
    private TableController tableController;

    @FXML
    public void initialize() {
        filteredSongs = new FilteredList<>(masterSongList, s -> true);
        TableSong.setItems(filteredSongs);

        musicPlayer = new MusicPlayerController(
                TableSong,
                currentlyPlaying,
                PlaybackSlider,
                PlayPauseButton,
                flacPlayer,
                sliderThreadHolder,
                stopSliderUpdaterWrapper,
                userDraggingSliderWrapper,
                filteredSongs
        );

        tableController = new TableController(
                TableSong,
                currentlyPlaying,
                PlaybackSlider,
                PlayPauseButton,
                flacPlayer,
                sliderThreadHolder,
                stopSliderUpdaterWrapper,
                userDraggingSliderWrapper
        );
        tableController.setupTableRows(CoverColumn);


        musicPlayer.setPlaybackMode(config.getString("playbackMode", "No"));

        // Configurar columnas de forma genérica
        setupColumn(ArtistColumn, ArtistColumnMenuItem, "showArtistColumn", "Ver Columna Artistas", "Ocultar Columna Artistas");
        setupColumn(AlbumColumn, AlbumColumnMenuItem, "showAlbumColumn", "Ver Columna Album", "Ocultar Columna Album");
        setupColumn(VersionsColumn, VersionsColumnMenuItem, "showVersionsColumn", "Ver Columna Versiones", "Ocultar Columna Versiones");
        setupColumn(RatingColumn, RatingColumnMenuItem, "showRatingColumn", "Ver Columna Rating", "Ocultar Columna Rating");
        setupColumn(CoverColumn, CoverColumnMenuItem, "showCoverColumn", "Ver Columna Cover", "Ocultar Covers");

        ratingManager.configureRatingColumn(RatingColumn);
        CoverColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCoverFile()));
        CoverColumn.setCellFactory(com.jumpcraft08.musiclibrary.view.RenderCover.create());

        currentlyPlaying.addListener((obs, oldSong, newSong) -> {
            NowPlayingLabel.setText(newSong != null ? "Reproduciendo: " + newSong.getFileName() : "Nada reproduciéndose");
            TableSong.refresh();
        });

        flacPlayer.setPlaybackListener(() -> Platform.runLater(musicPlayer::onSongFinished));

        setupPlaybackControls();
    }

    private void setupColumn(TableColumn<?, ?> column, MenuItem menuItem,
                             String configKey, String showText, String hideText) {
        boolean visible = config.getBoolean(configKey, true);
        column.setVisible(visible);
        menuItem.setText(visible ? hideText : showText);
    }

    @FXML
    private void toggleColumnVisibility(TableColumn<?, ?> column, MenuItem menuItem,
                                        String configKey, String showText, String hideText) {
        boolean newState = !column.isVisible();
        column.setVisible(newState);
        menuItem.setText(newState ? hideText : showText);
        config.setBoolean(configKey, newState);
    }

    @FXML public void HideArtistColumn() { toggleColumnVisibility(ArtistColumn, ArtistColumnMenuItem, "showArtistColumn", "Ver Columna Artistas", "Ocultar Columna Artistas"); }
    @FXML public void HideAlbumColumn() { toggleColumnVisibility(AlbumColumn, AlbumColumnMenuItem, "showAlbumColumn", "Ver Columna Album", "Ocultar Columna Album"); }
    @FXML public void HideVersionsColumn() { toggleColumnVisibility(VersionsColumn, VersionsColumnMenuItem, "showVersionsColumn", "Ver Columna Versiones", "Ocultar Columna Versiones"); }
    @FXML public void HideCoverColumn() { toggleColumnVisibility(CoverColumn, CoverColumnMenuItem, "showCoverColumn", "Ver Columna Cover", "Ocultar Columna Cover"); }
    @FXML public void HideRatingColumn() { toggleColumnVisibility(RatingColumn, RatingColumnMenuItem, "showRatingColumn", "Ver Columna Rating", "Ocultar Columna Rating"); }

    private void setupPlaybackControls() {
        PlayPauseButton.setOnAction(e -> musicPlayer.togglePlayPause());
        PlaybackSlider.setOnMousePressed(e -> userDraggingSliderWrapper[0] = true);
        PlaybackSlider.setOnMouseReleased(e -> {
            userDraggingSliderWrapper[0] = false;
            if (flacPlayer.getTotalSamples() > 0)
                flacPlayer.seek((long) PlaybackSlider.getValue());
        });
    }

    @FXML
    public void openSettingsWindow() {
        try {
            Parent root = FXMLLoader.load(App.class.getResource("/com/jumpcraft08/musiclibrary/SettingsView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajustes");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openSearchDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Canciones");
        dialog.setHeaderText("Ingrese un término para buscar canciones:");
        dialog.setContentText("Buscar (dejar vacío para limpiar filtro):");

        dialog.showAndWait().ifPresent(search -> {
            if (search.isBlank()) filteredSongs.setPredicate(s -> true);
            else {
                String lower = search.toLowerCase();
                filteredSongs.setPredicate(s -> s.getFileName().toLowerCase().contains(lower)
                        || s.getArtist().toLowerCase().contains(lower)
                        || s.getAlbum().toLowerCase().contains(lower));
            }
        });
    }

    @FXML
    public void SelectFolderController() {
        TableBundle bundle = new TableBundle(TableSong, FileNameColumn, ArtistColumn, AlbumColumn, VersionsColumn, RatingColumn);
        Map<TypeFile, File> folders = SelectFolder.select(bundle, TypeFile.FLAC_HI_RES, TypeFile.FLAC_CD, TypeFile.M4A);

        if (folders != null) {
            List<SongFile> loaded = FileTableLoader.loadSongs(folders);
            masterSongList.setAll(TablePopulator.populateTable(bundle, loaded));
        }
    }

    public void shutdown() {
        try {
            flacPlayer.stop();
            System.out.println("Reproductor detenido correctamente.");
        } catch (Exception e) {
            System.err.println("Error al cerrar el reproductor: " + e.getMessage());
        }
    }

    public void playPlaylist(Playlist playlist) {
        musicPlayer.playPlaylist(playlist);
    }
}
