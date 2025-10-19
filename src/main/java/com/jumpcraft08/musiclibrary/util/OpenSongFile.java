package com.jumpcraft08.musiclibrary.util;

import com.jumpcraft08.musiclibrary.view.DialogManager;
import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.view.FlacPlayer;

import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;

import java.io.File;

public class OpenSongFile {
    public static void openSong(SongFile song, FlacPlayer player, Slider slider, Button playPauseButton,
                                Thread[] sliderThreadHolder, boolean[] stopFlag, boolean[] userDraggingSlider) {
        if (song == null || player == null || slider == null || playPauseButton == null) return;

        try {
            // Detener reproducción previa
            player.stop();
            if (sliderThreadHolder[0] != null && sliderThreadHolder[0].isAlive()) stopFlag[0] = true;

            File file = song.getPreferredFile();
            if (file == null || !file.exists()) {
                DialogManager.showError("Fallo al abrir", "Archivo no encontrado",
                        "No se encontró el archivo preferente para: " + song.getFileName(), null);
                return;
            }

            player.open(file);

            // Configurar slider
            slider.setMin(0);
            slider.setMax(player.getTotalSamples());
            slider.setValue(0);
            playPauseButton.setText("Pause");

            // Iniciar hilo de actualización
            stopFlag[0] = false;
            sliderThreadHolder[0] = new Thread(() -> {
                while (!stopFlag[0] && (player.isPlaying() || player.getCurrentSample() < player.getTotalSamples())) {
                    if (!userDraggingSlider[0]) {
                        final double pos = player.getCurrentSample();
                        Platform.runLater(() -> slider.setValue(pos));
                    }
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            });
            sliderThreadHolder[0].setDaemon(true);
            sliderThreadHolder[0].start();

            player.play();

        } catch (Exception e) {
            DialogManager.showError("Fallo al abrir", "No se pudo abrir la canción",
                    "Error al abrir: " + song.getFileName(), e);
        }
    }


    public static void DefaultopenSong(SongFile song) {
        if (song == null) return;
        File file = song.getPreferredFile();
        if (file != null && file.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception e) {

                DialogManager.showError("Fallo al abrir", "El archivo no es valido", "No se pudo abrir el archivo: " + file.getAbsolutePath(), e);
            }
        } else {
            DialogManager.showError("Fallo al abrir", "Archivo no encontrado", "No se encontró el archivo preferente para: " + song.getFileName(), null);
        }
    }
}
