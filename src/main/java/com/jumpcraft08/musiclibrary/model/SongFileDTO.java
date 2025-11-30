package com.jumpcraft08.musiclibrary.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SongFileDTO {
    public String fileName;
    public String artist;
    public String album;
    public int rating;
    public String preferredFilePath;
    public String coverFilePath;
    public List<String> versions;
    public long lastModified;

    public SongFileDTO() {} // Constructor vacío necesario para Gson

    public SongFileDTO(SongFile song) {
        this.fileName = song.getFileName();
        this.artist = song.getArtist();
        this.album = song.getAlbum();
        this.rating = song.getRating();
        this.preferredFilePath = song.getPreferredFile() != null ? song.getPreferredFile().getAbsolutePath() : null;
        this.coverFilePath = song.getCoverFile() != null ? song.getCoverFile().getAbsolutePath() : null;
        this.lastModified = song.getPreferredFile() != null ? song.getPreferredFile().lastModified() : 0;
        String versionsStr = song.getVersionsAsString();
        this.versions = versionsStr.isEmpty() ? new ArrayList<>() : Arrays.asList(versionsStr.split(",\\s*"));
    }

    public SongFile toSongFile() {
        File prefFile = preferredFilePath != null ? new File(preferredFilePath) : null;
        SongFile song;

        if (prefFile != null && prefFile.exists() && prefFile.lastModified() == lastModified) {
            // Archivo no modificado: usar constructor de cache
            song = new SongFile(prefFile, artist, album, rating,
                    coverFilePath != null ? new File(coverFilePath) : null,
                    lastModified);
        } else {
            // Archivo modificado o no existe: leer tags de nuevo
            song = new SongFile(prefFile != null ? prefFile : new File(fileName));
            song.setRating(rating);
            song.setCoverFile(coverFilePath != null ? new File(coverFilePath) : null);
        }

        // Restaurar versiones
        if (versions != null) {
            versions.forEach(v -> {
                try {
                    song.addVersion(TypeFile.valueOf(v));
                } catch (Exception ignored) {}
            });
        }

        return song;
    }
}
