package com.jumpcraft08.musiclibrary.util;

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

    public void open(File file) throws IOException, LineUnavailableException {
        stop(); // detener cualquier reproducción previa

        decoder = new FlacDecoder(file);
        while (decoder.readAndHandleMetadataBlock() != null) {}
        info = decoder.streamInfo;
        buffer = new int[info.numChannels][65536];

        currentSample = 0;

        startPlaybackThread();
    }

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

                            if (info.sampleDepth == 24) {
                                val = val / 256; // escalar 24-bit a 16-bit
                            }

                            // Saturar a 16-bit
                            if (val > 32767) val = 32767;
                            if (val < -32768) val = -32768;

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

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public synchronized void togglePlayPause() {
        playing = !playing;
    }

    public synchronized void play() {
        playing = true;
    }

    public synchronized void pause() {
        playing = false;
    }

    public boolean isPlaying() {
        return playing;
    }

    public long getCurrentSample() {
        return currentSample;
    }

    public long getTotalSamples() {
        return info != null ? info.numSamples : 0;
    }

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
