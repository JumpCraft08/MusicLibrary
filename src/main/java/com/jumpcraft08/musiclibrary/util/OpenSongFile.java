package com.jumpcraft08.musiclibrary.util;

import com.jumpcraft08.musiclibrary.model.SongFile;

import java.io.File;

public class OpenSongFile {
    public static void openSong(SongFile song) {
        if (song == null) return;
        File file = song.getPreferredFile();
        if (file != null && file.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception e) {
                System.out.println("No se pudo abrir el archivo: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        } else {
            System.out.println("No se encontró el archivo preferente para: " + song.getFileName());
        }
    }
}
