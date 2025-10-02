package com.jumpcraft08.musiclibrary;

import javafx.beans.property.SimpleStringProperty;

public class SongFile {
private final SimpleStringProperty FileName;

    public SongFile(String FileName) {
        this.FileName = new SimpleStringProperty(FileName);
    }

    public String getFileName() {
        return FileName.get();
    }
}
