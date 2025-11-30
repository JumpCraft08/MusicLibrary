package com.jumpcraft08.musiclibrary.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SongFile {

    private final SimpleStringProperty fileName;
    private final SimpleStringProperty artist;
    private final SimpleStringProperty album;
    private final Set<TypeFile> versions = EnumSet.noneOf(TypeFile.class);
    private File preferredFile;
    private File coverFile;
    private final SimpleIntegerProperty rating = new SimpleIntegerProperty(-1);
    private long lastModified;

    /** Constructor normal: lee los tags del archivo */
    public SongFile(File file) {
        this.fileName = new SimpleStringProperty(file.getName());
        this.preferredFile = file;

        Tag tag = readTag(file);
        this.artist = new SimpleStringProperty(tag != null ? tag.getFirst("ARTIST") : "Unknown Artist");
        this.album = new SimpleStringProperty(tag != null ? tag.getFirst("ALBUM") : "Unknown Album");

        updateLastModified();
    }

    /** Constructor especial para cache: no lee tags, usa los valores guardados */
    public SongFile(File file, String artist, String album, int rating, File coverFile, long lastModified) {
        this.fileName = new SimpleStringProperty(file.getName());
        this.preferredFile = file;
        this.artist = new SimpleStringProperty(artist);
        this.album = new SimpleStringProperty(album);
        this.rating.set(rating);
        this.coverFile = coverFile;
        this.lastModified = lastModified;
    }

    private static Tag readTag(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            return audioFile.getTag();
        } catch (Exception e) {
            return null;
        }
    }

    // Getters
    public String getFileName() { return fileName.get(); }
    public String getArtist() { return artist.get(); }
    public String getAlbum() { return album.get(); }
    public int getRating() { return rating.get(); }
    public File getPreferredFile() { return preferredFile; }
    public File getCoverFile() { return coverFile; }
    public long getLastModified() { return lastModified; }

    // Setters
    public void setCoverFile(File coverFile) { this.coverFile = coverFile; }
    public void setRating(int rating) { this.rating.set(rating); }
    public void setPreferredFile(File preferredFile) { this.preferredFile = preferredFile; }
    public void updateLastModified() {
        if (preferredFile != null) lastModified = preferredFile.lastModified();
    }

    // Versiones
    public void addVersion(TypeFile typeFile) { versions.add(typeFile); }
    public String getVersionsAsString() {
        return versions.isEmpty() ? "" : versions.stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    // Propiedades JavaFX
    public SimpleIntegerProperty ratingProperty() { return rating; }
}
