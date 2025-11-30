package com.jumpcraft08.flac_implementation.utils;

import com.jumpcraft08.musiclibrary.view.DialogManager;
import com.jumpcraft08.musiclibrary.model.SongFile;
import java.io.File;

public class OpenSongDefault {

    public static void openDefaultSong(SongFile song) {
        if (song == null) return;
        File file = song.getPreferredFile();
        if (file != null && file.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception e) {
                DialogManager.showError("Fallo al abrir", "El archivo no es válido",
                        "No se pudo abrir el archivo: " + file.getAbsolutePath(), e);
            }
        } else {
            DialogManager.showError("Fallo al abrir", "Archivo no encontrado",
                    "No se encontró el archivo preferente para: " + song.getFileName(), null);
        }
    }
}
