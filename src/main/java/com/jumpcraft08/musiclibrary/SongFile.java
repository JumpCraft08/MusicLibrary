package com.jumpcraft08.musiclibrary;

import javafx.beans.property.SimpleStringProperty;

public class SongFile {
private final SimpleStringProperty FileName;
private final SimpleStringProperty Artist;

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
}
