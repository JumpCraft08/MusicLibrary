package com.jumpcraft08.musiclibrary.model;

import java.io.File;

public enum TypeFile {
    M4A(3, ".m4a"),
    FLAC_CD(1, ".flac"),
    FLAC_HI_RES(2, ".flac");

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

    public static TypeFile fromFile(File file) {
        String name = file.getName().toLowerCase();
        for (TypeFile type : values()) {
            if (name.endsWith(type.getExtension().toLowerCase())) {
                return type;
            }
        }
        return null;
    }
}
