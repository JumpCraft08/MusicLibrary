package com.jumpcraft08.musiclibrary.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jumpcraft08.musiclibrary.model.Playlist;
import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.PlaylistJson;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {

    private static final String PLAYLIST_FILE = "playlists.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final List<Playlist> playlists = new ArrayList<>();

    public PlaylistManager() {
        loadPlaylists();
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
        savePlaylists();
    }

    public void addSongToPlaylist(SongFile song, Playlist playlist) {
        if (!playlist.getSongs().contains(song)) {
            playlist.addSong(song);
            savePlaylists();
        }
    }

    /** Guarda playlists y sus canciones */
    public void savePlaylists() {
        List<PlaylistJson> jsonPlaylists = new ArrayList<>();
        for (Playlist p : playlists) {
            PlaylistJson pj = new PlaylistJson();
            pj.name = p.getName();
            pj.songPaths = new ArrayList<>();
            for (SongFile song : p.getSongs()) {
                if (song.getPreferredFile() != null)
                    pj.songPaths.add(song.getPreferredFile().getAbsolutePath());
            }
            jsonPlaylists.add(pj);
        }

        try (Writer writer = new FileWriter(PLAYLIST_FILE)) {
            gson.toJson(jsonPlaylists, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Carga playlists desde JSON, maneja songPaths null (compatibilidad con archivos antiguos) */
    public void loadPlaylists() {
        File file = new File(PLAYLIST_FILE);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            PlaylistJson[] loaded = gson.fromJson(reader, PlaylistJson[].class);
            if (loaded != null) {
                playlists.clear();
                for (PlaylistJson pj : loaded) {
                    Playlist p = new Playlist(pj.name);
                    if (pj.songPaths != null) {
                        for (String path : pj.songPaths) {
                            File f = new File(path);
                            if (f.exists()) {
                                p.addSong(new SongFile(f));
                            }
                        }
                    }
                    playlists.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
