package com.jumpcraft08.file_manager.util;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.TypeFile;
import com.jumpcraft08.musiclibrary.util.RatingManager;
import com.jumpcraft08.musiclibrary.util.JsonCache;


import java.io.File;
import java.util.*;


public class FileTableLoader {


    private static final int SEARCH_DEPTH = 3;


    public static List<SongFile> loadSongs(Map<TypeFile, File> versionFolders) {
        if (versionFolders == null) return new ArrayList<>();


        List<SongFile> cached = JsonCache.load();
        if (cached != null && !cached.isEmpty()) return cached;


        Map<String, SongFile> songMap = new HashMap<>();
        RatingManager ratingManager = new RatingManager();


        versionFolders.forEach((typeFile, folder) -> {
            if (!folder.exists() || !folder.isDirectory()) return;


            List<Map<File, File>> filesWithCovers = ListFiles.listFilesWithCover(folder, SEARCH_DEPTH, typeFile.getExtension());
            filesWithCovers.forEach(map -> map.forEach((musicFile, cover) ->
                    processFile(songMap, musicFile, cover, typeFile, ratingManager)));
        });


        List<SongFile> songList = new ArrayList<>(songMap.values());
        JsonCache.save(songList);
        return songList;
    }


    private static void processFile(Map<String, SongFile> songMap, File musicFile, File cover,
                                    TypeFile typeFile, RatingManager ratingManager) {
        String baseName = getBaseName(musicFile);


        SongFile song = songMap.computeIfAbsent(baseName, name -> {
            SongFile s = new SongFile(musicFile);
            s.setRating(ratingManager.getRating(name));
            return s;
        });


        if (song.getCoverFile() == null && cover != null) {
            song.setCoverFile(cover);
        }


        song.addVersion(typeFile);
        updatePreferredFile(song, musicFile, typeFile);
    }


    private static String getBaseName(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return dotIndex > 0 ? name.substring(0, dotIndex) : name;
    }


    private static void updatePreferredFile(SongFile song, File musicFile, TypeFile typeFile) {
        File currentPreferred = song.getPreferredFile();
        if (currentPreferred == null) {
            song.setPreferredFile(musicFile);
        } else {
            TypeFile currentType = TypeFile.fromFile(currentPreferred);
            if (currentType != null && typeFile.getPriority() < currentType.getPriority()) {
                song.setPreferredFile(musicFile);
            }
        }
    }
}
