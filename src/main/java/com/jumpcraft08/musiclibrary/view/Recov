package com.jumpcraft08.musiclibrary.view;

import io.nayuki.flac.decode.FlacDecoder;
import io.nayuki.flac.common.StreamInfo;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class FlacPlayerWindow {

    private JFrame frame;
    private JSlider slider;
    private JButton playPauseButton;
    private volatile boolean playing = false;

    private SourceDataLine line;
    private FlacDecoder decoder;
    private StreamInfo info;
    private int[][] buffer;

    private Thread playbackThread;
    private long currentSample = 0;
    private volatile boolean stopRequested = false;

    public FlacPlayerWindow(File file) {
        try {
            decoder = new FlacDecoder(file);
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
        slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (info != null && info.numSamples > 0) {
                    long targetSample = (long) ((double) slider.getValue() / slider.getMaximum() * info.numSamples);
                    seekTo(targetSample);
                }
            }
        });

        frame.add(playPauseButton, BorderLayout.WEST);
        frame.add(slider, BorderLayout.CENTER);
        frame.setSize(600, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopPlayback();
            }
        });
        frame.setVisible(true);
    }

    private synchronized void togglePlayPause() {
        if (!playing) {
            playing = true;
            playPauseButton.setText("Pause");
            if (playbackThread == null || !playbackThread.isAlive()) startPlayback();
        } else {
            playing = false;
            playPauseButton.setText("Play");
        }
    }

    private void startPlayback() {
        stopRequested = false;
        playbackThread = new Thread(() -> {
            try {
                // Forzamos 16 bits para reproducir en Java Sound
                AudioFormat format = new AudioFormat(info.sampleRate, 16, info.numChannels, true, false);
                line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();

                int bytesPerSample = 2;
                byte[] audioBytes = new byte[buffer[0].length * info.numChannels * bytesPerSample];

                while (!stopRequested) {
                    if (!playing) {
                        Thread.sleep(10);
                        continue;
                    }

                    int read;
                    synchronized (decoder) {
                        read = decoder.readAudioBlock(buffer, 0);
                    }
                    if (read == 0) break;

                    for (int i = 0, k = 0; i < read; i++) {
                        for (int ch = 0; ch < info.numChannels; ch++) {
                            int val = buffer[ch][i];

                            if (info.sampleDepth == 24) {
                                // Escalado de 24-bit a 16-bit, manteniendo rango dinámico
                                val = val / 256; // opción simple: >>8 también funciona
                                // Alternativa más precisa:
                                // val = (int) Math.round(val * 32767.0 / 8388607.0);
                            }

                            // Saturar para no exceder rango 16-bit
                            if (val > 32767) val = 32767;
                            if (val < -32768) val = -32768;

                            audioBytes[k++] = (byte) (val & 0xFF);
                            audioBytes[k++] = (byte) ((val >> 8) & 0xFF);
                        }
                    }

                    line.write(audioBytes, 0, read * info.numChannels * bytesPerSample);
                    currentSample += read;

                    if (!slider.getValueIsAdjusting()) {
                        final int pos = (int) ((double) currentSample / info.numSamples * slider.getMaximum());
                        SwingUtilities.invokeLater(() -> slider.setValue(pos));
                    }
                }

                line.drain();
                line.stop();
                line.close();

            } catch (IOException | LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        playbackThread.start();
    }

    private void seekTo(long targetSample) {
        try {
            synchronized (decoder) {
                decoder.seekAndReadAudioBlock(targetSample, buffer, 0);
            }
            currentSample = targetSample;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlayback() {
        stopRequested = true;
        playing = false;
        playPauseButton.setText("Play");
        if (line != null) {
            line.stop();
            line.close();
        }
        if (decoder != null) {
            try {
                decoder.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
