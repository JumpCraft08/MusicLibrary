package com.jumpcraft08.musiclibrary.model;

import com.jumpcraft08.musiclibrary.model.TypeFile;

import javafx.beans.property.SimpleStringProperty;

import java.util.Set;
import java.util.EnumSet;

public class SongFile {
private final SimpleStringProperty FileName;
private final SimpleStringProperty Artist;
private final Set<TypeFile> Versions = EnumSet.noneOf(TypeFile.class);

    public SongFile(String FileName, String Artist) {
        this.FileName = new SimpleStringProperty(FileName);
        this.Artist = new SimpleStringProperty(Artist);
    }

    public String getFileName() {
        return FileName.get();
    }

    public String getArtist() {
        return Artist.get();
    }

    public Set<TypeFile> getVersions() {
        return Versions;
    }

    public void addVersion(TypeFile typeFile) {
        Versions.add(typeFile);
    }

    public String getVersionsAsString() {
        if (Versions.isEmpty()) return "";
        return String.join(", ", Versions.stream().map(Enum::name).toList());
    }
}
