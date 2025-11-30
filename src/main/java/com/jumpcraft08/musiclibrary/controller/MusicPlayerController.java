package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.model.Playlist;
import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.flac_implementation.player.FlacPlayer;
import com.jumpcraft08.flac_implementation.utils.OpenSongFile;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;

public class MusicPlayerController {

    private final TableView<SongFile> table;
    private final ObjectProperty<SongFile> currentlyPlaying;
    private final Slider playbackSlider;
    private final Button playPauseButton;
    private final FlacPlayer flacPlayer;
    private final Thread[] sliderThreadHolder;
    private final boolean[] stopSliderUpdaterWrapper;
    private final boolean[] userDraggingSliderWrapper;

    private final ObservableList<SongFile> playlist = FXCollections.observableArrayList();
    private int playlistIndex = 0;

    private enum PlaybackMode { NO, CONTINUA, PLAYLIST }
    private PlaybackMode playbackMode = PlaybackMode.NO;

    private FilteredList<SongFile> filteredSongs;

    public MusicPlayerController(
            TableView<SongFile> table,
            ObjectProperty<SongFile> currentlyPlaying,
            Slider playbackSlider,
            Button playPauseButton,
            FlacPlayer flacPlayer,
            Thread[] sliderThreadHolder,
            boolean[] stopSliderUpdaterWrapper,
            boolean[] userDraggingSliderWrapper,
            FilteredList<SongFile> filteredSongs
    ) {
        this.table = table;
        this.currentlyPlaying = currentlyPlaying;
        this.playbackSlider = playbackSlider;
        this.playPauseButton = playPauseButton;
        this.flacPlayer = flacPlayer;
        this.sliderThreadHolder = sliderThreadHolder;
        this.stopSliderUpdaterWrapper = stopSliderUpdaterWrapper;
        this.userDraggingSliderWrapper = userDraggingSliderWrapper;
        this.filteredSongs = filteredSongs;
    }

    public void setPlaybackMode(String mode) {
        try {
            playbackMode = (mode == null) ? PlaybackMode.NO : PlaybackMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            playbackMode = PlaybackMode.NO;
        }
    }

    public void togglePlayPause() {
        if (flacPlayer.isPlaying()) {
            flacPlayer.pause();
            playPauseButton.setText("Play");
        } else {
            flacPlayer.play();
            playPauseButton.setText("Pause");
        }
    }

    public void onSongFinished() {
        switch (playbackMode) {
            case CONTINUA -> playNext(filteredSongs);
            case PLAYLIST -> playNext(playlist);
            case NO -> { }
        }
    }

    private void playNext(ObservableList<SongFile> list) {
        if (list.isEmpty() || currentlyPlaying.get() == null) return;

        int index = list.indexOf(currentlyPlaying.get());
        int nextIndex = (index + 1 < list.size()) ? index + 1 : 0;
        SongFile nextSong = list.get(nextIndex);

        table.getSelectionModel().select(nextSong);
        currentlyPlaying.set(nextSong);
        openSong(nextSong);
    }

    public void playPlaylist(Playlist playlistToPlay) {
        if (playlistToPlay == null || playlistToPlay.getSongs().isEmpty()) return;

        playlist.setAll(playlistToPlay.getSongs());
        playlistIndex = 0;
        playbackMode = PlaybackMode.PLAYLIST;

        SongFile firstSong = playlist.get(0);
        table.getSelectionModel().select(firstSong);
        currentlyPlaying.set(firstSong);
        openSong(firstSong);
    }

    private void openSong(SongFile song) {
        OpenSongFile.openSong(song, flacPlayer, playbackSlider, playPauseButton,
                sliderThreadHolder, stopSliderUpdaterWrapper, userDraggingSliderWrapper);
    }

    public ObservableList<SongFile> getPlaylist() {
        return playlist;
    }

    public void setFilteredSongs(FilteredList<SongFile> filteredSongs) {
        this.filteredSongs = filteredSongs;
    }
}
