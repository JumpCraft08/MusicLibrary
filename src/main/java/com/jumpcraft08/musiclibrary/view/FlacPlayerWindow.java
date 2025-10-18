package com.jumpcraft08.musiclibrary.view;

import io.nayuki.flac.decode.FlacDecoder;
import io.nayuki.flac.common.StreamInfo;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FlacPlayerWindow {

    private JFrame frame;
    private JSlider slider;
    private JButton playPauseButton;
    private volatile boolean playing = false;
    private volatile boolean seeking = false;

    private SourceDataLine line;
    private FlacDecoder decoder;
    private StreamInfo info;
    private int[][] buffer;

    private Thread playbackThread;
    private long currentSample = 0;

    public FlacPlayerWindow(File file) {
        try {
            decoder = new FlacDecoder(file);

            // Leer todos los metadatos antes de hacer cualquier otra cosa
            while (decoder.readAndHandleMetadataBlock() != null) {}

            info = decoder.streamInfo;
            buffer = new int[info.numChannels][65536];

            setupUI(file.getName());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "No se pudo abrir el archivo FLAC:\n" + e.getMessage());
        }
    }

    private void setupUI(String title) {
        frame = new JFrame("FLAC Player - " + title);
        frame.setLayout(new BorderLayout());

        playPauseButton = new JButton("Play");
        playPauseButton.addActionListener(e -> togglePlayPause());

        slider = new JSlider(0, 1000, 0);
        slider.addChangeListener(new SliderListener());

        frame.add(playPauseButton, BorderLayout.WEST);
        frame.add(slider, BorderLayout.CENTER);
        frame.setSize(600, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private synchronized void togglePlayPause() {
        if (!playing) {
            playing = true;
            playPauseButton.setText("Pause");

            // Inicia el hilo solo si aún no existe o terminó
            if (playbackThread == null || !playbackThread.isAlive()) {
                startPlayback();
            }
        } else {
            playing = false;
            playPauseButton.setText("Play");
        }
    }

    private void startPlayback() {
        playbackThread = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(
                        info.sampleRate,
                        info.sampleDepth,
                        info.numChannels,
                        true,
                        false
                );

                line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();

                int bytesPerSample = info.sampleDepth / 8;
                byte[] audioBytes = new byte[buffer[0].length * info.numChannels * bytesPerSample];

                while (true) {
                    if (!playing) {
                        Thread.sleep(10); // espera hasta que se presione Play
                        continue;
                    }

                    if (seeking) {
                        Thread.sleep(10);
                        continue;
                    }

                    int read;
                    synchronized (decoder) { // Evita concurrencia con seek
                        read = decoder.readAudioBlock(buffer, 0);
                    }

                    if (read == 0) break;

                    // Convertir int[][] a byte[]
                    for (int i = 0, k = 0; i < read; i++) {
                        for (int ch = 0; ch < info.numChannels; ch++) {
                            int val = buffer[ch][i];
                            for (int b = 0; b < bytesPerSample; b++, k++) {
                                audioBytes[k] = (byte) (val >>> (b * 8));
                            }
                        }
                    }

                    line.write(audioBytes, 0, read * info.numChannels * bytesPerSample);
                    currentSample += read;

                    final int sliderPos = (int) ((double) currentSample / info.numSamples * slider.getMaximum());
                    SwingUtilities.invokeLater(() -> slider.setValue(sliderPos));
                }

                line.drain();
                line.stop();
                line.close();

                synchronized (decoder) {
                    decoder.close();
                }

            } catch (IOException | LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        playbackThread.start();
    }

    private void seekTo(long targetSample) {
        seeking = true;
        try {
            synchronized (decoder) { // Bloquea el decoder para que no haya concurrencia
                decoder.seekAndReadAudioBlock(targetSample, buffer, 0);
            }
            currentSample = targetSample;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            seeking = false;
        }
    }

    private class SliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (slider.getValueIsAdjusting()) {
                long targetSample = (long) ((double) slider.getValue() / slider.getMaximum() * info.numSamples);
                seekTo(targetSample);
            }
        }
    }
}
