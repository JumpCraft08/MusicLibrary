package com.jumpcraft08.flac_implementation.player;

import io.nayuki.flac.decode.FlacDecoder;
import io.nayuki.flac.common.StreamInfo;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class FlacPlayer {

    private FlacDecoder decoder;
    private StreamInfo info;
    private int[][] buffer;
    private SourceDataLine line;

    private volatile boolean playing = false;
    private volatile boolean stopRequested = false;
    private long currentSample = 0;

    private Thread playbackThread;

    // Listener para notificar fin de reproducción
    public interface PlaybackListener {
        void onPlaybackFinished();
    }

    private PlaybackListener playbackListener;

    public void setPlaybackListener(PlaybackListener listener) {
        this.playbackListener = listener;
    }

    // Abrir archivo FLAC
    public void open(File file) throws IOException, LineUnavailableException {
        stop(); // detener cualquier reproducción previa

        decoder = new FlacDecoder(file);
        while (decoder.readAndHandleMetadataBlock() != null) {} // leer metadata
        info = decoder.streamInfo;
        buffer = new int[info.numChannels][65536];

        currentSample = 0;
        startPlaybackThread();
    }

    // Hilo de reproducción
    private void startPlaybackThread() throws LineUnavailableException {
        stopRequested = false;
        playing = false;

        AudioFormat format = new AudioFormat(info.sampleRate, 16, info.numChannels, true, false);
        line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();

        playbackThread = new Thread(() -> {
            try {
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
                            if (info.sampleDepth == 24) val /= 256;
                            val = Math.max(-32768, Math.min(32767, val));

                            audioBytes[k++] = (byte) (val & 0xFF);
                            audioBytes[k++] = (byte) ((val >> 8) & 0xFF);
                        }
                    }

                    line.write(audioBytes, 0, read * info.numChannels * bytesPerSample);
                    currentSample += read;
                }

                line.drain();
                line.stop();
                line.close();

                // Notificar fin de reproducción si no se detuvo manualmente
                if (playbackListener != null && !stopRequested) {
                    playbackListener.onPlaybackFinished();
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    // Controles
    public synchronized void togglePlayPause() { playing = !playing; }
    public synchronized void play() { playing = true; }
    public synchronized void pause() { playing = false; }

    public boolean isPlaying() { return playing; }

    public long getCurrentSample() { return currentSample; }
    public long getTotalSamples() { return info != null ? info.numSamples : 0; }

    public void seek(long targetSample) {
        try {
            synchronized (decoder) {
                decoder.seekAndReadAudioBlock(targetSample, buffer, 0);
            }
            currentSample = targetSample;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        stopRequested = true;
        playing = false;
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
