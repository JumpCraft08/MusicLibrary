package com.jumpcraft08.musiclibrary.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.SongFileDTO;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase para guardar y cargar cache de canciones en JSON.
 * Usa SongFileDTO con lastModified para detectar cambios en archivos.
 */
public class JsonCache {

    private static final String CACHE_FILE = "songs_cache.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /** Guarda la lista de canciones en cache */
    public static void save(List<SongFile> songs) {
        // Cargar cache existente para preservar ratings
        List<SongFile> existing = load();
        Map<String, Integer> existingRatings = new HashMap<>();
        if (existing != null) {
            for (SongFile s : existing) {
                existingRatings.put(s.getFileName(), s.getRating());
            }
        }

        // Fusionar ratings existentes
        for (SongFile song : songs) {
            if (existingRatings.containsKey(song.getFileName())) {
                song.setRating(existingRatings.get(song.getFileName()));
            }
        }

        // Convertir a DTO y guardar
        List<SongFileDTO> dtoList = new ArrayList<>();
        for (SongFile s : songs) dtoList.add(new SongFileDTO(s));

        try (Writer writer = new FileWriter(CACHE_FILE)) {
            gson.toJson(dtoList, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Actualiza solo el rating de un archivo en cache */
    public static void updateSongRating(String fileName, int rating) {
        File file = new File(CACHE_FILE);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            SongFileDTO[] dtoArray = gson.fromJson(reader, SongFileDTO[].class);
            for (SongFileDTO dto : dtoArray) {
                if (dto.fileName.equals(fileName)) {
                    dto.rating = rating;
                    break;
                }
            }

            try (Writer writer = new FileWriter(file)) {
                gson.toJson(dtoArray, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Carga la lista de canciones desde la cache */
    public static List<SongFile> load() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) return null;

        try (Reader reader = new FileReader(file)) {
            SongFileDTO[] dtoArray = gson.fromJson(reader, SongFileDTO[].class);
            List<SongFile> songs = new ArrayList<>();
            for (SongFileDTO dto : dtoArray) {
                File prefFile = dto.preferredFilePath != null ? new File(dto.preferredFilePath) : null;
                if (prefFile == null || !prefFile.exists() || prefFile.lastModified() != dto.lastModified) {
                    // Archivo modificado o no existe: ignorar para reconstrucción
                    continue;
                }
                songs.add(dto.toSongFile());
            }
            return songs.isEmpty() ? null : songs;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
