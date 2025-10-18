package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.TableBundle;
import com.jumpcraft08.musiclibrary.model.TypeFile;
import com.jumpcraft08.musiclibrary.controller.ListFiles;
import com.jumpcraft08.musiclibrary.util.RatingManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;

public class TablePopulator {

    private static final int SEARCH_DEPTH = 3;

    public static void populateTable(TableBundle bundle, Map<TypeFile, File> versionFolders) {
        if (bundle == null || versionFolders == null) return;

        RatingManager ratingManager = new RatingManager();

        Map<String, SongFile> songMap = new HashMap<>();

        for (Map.Entry<TypeFile, File> entry : versionFolders.entrySet()) {
            TypeFile typeFile = entry.getKey();
            File folder = entry.getValue();

            if (!folder.exists() || !folder.isDirectory()) continue;

            List<Map<File, File>> musicFilesWithCovers = ListFiles.listFilesWithCover(folder, SEARCH_DEPTH, typeFile.getExtension());

            for (Map<File, File> map : musicFilesWithCovers) {
                for (Map.Entry<File, File> fileEntry : map.entrySet()) {
                    File musicFile = fileEntry.getKey();
                    File cover = fileEntry.getValue();

                    String fileName = musicFile.getName();
                    String nameWithoutExtension = fileName.contains(".")
                            ? fileName.substring(0, fileName.lastIndexOf('.'))
                            : fileName;

                    SongFile song = songMap.get(nameWithoutExtension);
                    if (song == null) {
                        song = new SongFile(nameWithoutExtension, "Desconocido");

                        int rating = ratingManager.getRating(nameWithoutExtension);
                        song.setRating(rating);

                        songMap.put(nameWithoutExtension, song);
                    }

                    if (song.getCoverFile() == null && cover != null) {
                        song.setCoverFile(cover);
                    }

                    song.addVersion(typeFile);

                    File currentPreferred = song.getPreferredFile();
                    if (currentPreferred == null) {
                        song.setPreferredFile(musicFile);
                    } else {
                        TypeFile currentPreferredType = TypeFile.fromFile(currentPreferred);
                        if (currentPreferredType != null && typeFile.getPriority() < currentPreferredType.getPriority()) {
                            song.setPreferredFile(musicFile);
                        }
                    }

                }
            }
        }

        List<SongFile> sortedList = new ArrayList<>(songMap.values());
        sortedList.sort(Comparator.comparing(SongFile::getFileName));

        ObservableList<SongFile> songList = FXCollections.observableArrayList(sortedList);

        bundle.fileNameColumn().setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFileName())
        );

        bundle.artistColumn().setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getArtist())
        );

        bundle.versionsColumn().setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getVersionsAsString())
        );

        bundle.ratingColumn().setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getRating())
        );

        bundle.table().setItems(songList);
    }
}
