package com.jumpcraft08.musiclibrary.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class Playlist {
    private String name; // Gson serializa normalmente
    private transient ObservableList<SongFile> songs = FXCollections.observableArrayList(); // No serializable por Gson

    public Playlist(String name) {
        this.name = name;
    }

    public Playlist() {
        this("");
    }

    // Getter / Setter para Gson
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // JavaFX property para binding
    public StringProperty nameProperty() {
        return new SimpleStringProperty(this, "name", name);
    }

    // Observable list para UI
    public ObservableList<SongFile> getSongs() { return songs; }

    public void addSong(SongFile song) { songs.add(song); }
    public void removeSong(SongFile song) { songs.remove(song); }

    // Métodos auxiliares para serialización manual de la lista
    public List<SongFile> getSongsAsList() {
        return List.copyOf(songs);
    }

    public void setSongsFromList(List<SongFile> list) {
        songs = FXCollections.observableArrayList(list);
    }
}
