package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;

import java.io.File;

public class SongDetails {
    public static void showSongDetails(SongFile song) {
        StringBuilder info = new StringBuilder();
        info.append("Nombre: ").append(song.getFileName()).append("\n");
        info.append("Artista: ").append(song.getArtist()).append("\n");
        info.append("Versiones: ").append(song.getVersionsAsString()).append("\n");
        File preferred = song.getPreferredFile();
        info.append("Archivo preferido: ").append(preferred != null ? preferred.getAbsolutePath() : "Ninguno").append("\n");
        File cover = song.getCoverFile();
        info.append("Cover: ").append(cover != null ? cover.getAbsolutePath() : "Ninguno");


        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de la canción");
        alert.setHeaderText(null);
        alert.setContentText(info.toString());
        alert.showAndWait();
    }
}
