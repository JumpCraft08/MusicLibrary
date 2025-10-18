package com.jumpcraft08.musiclibrary.util;

import com.jumpcraft08.musiclibrary.view.DialogManager;
import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.view.FlacPlayerWindow;

import java.io.File;

public class OpenSongFile {
    public static void openSong(SongFile song) {
        if (song == null) return;
        File file = song.getPreferredFile();
        if (file != null && file.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(file);
                new FlacPlayerWindow(file);
            } catch (Exception e) {
                DialogManager.showError("Fallo al abrir", "El archivo no es valido", "No se pudo abrir el archivo: " + file.getAbsolutePath(), e);
            }
        } else {
            DialogManager.showError("Fallo al abrir", "Archivo no encontrado", "No se encontró el archivo preferente para: " + song.getFileName(), null);
        }
    }
}
