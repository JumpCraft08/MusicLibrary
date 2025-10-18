package com.jumpcraft08.musiclibrary.model;

public enum TypeFile {
    M4A(3, ".m4a"),
    FLAC_CD(1, ".flac"),
    FLAC_HI_RES(2, "flac");

    private final int priority;
    private final String extension;

    TypeFile(int priority, String extension) {
        this.priority = priority;
        this.extension = extension;
    }

    public int getPriority() {
        return priority;
    }

    public String getExtension() {
        return extension;
    }
}
