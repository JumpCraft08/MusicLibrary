package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.musiclibrary.model.TableBundle;
import com.jumpcraft08.musiclibrary.model.TypeFile;

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

    public static void populateTable(TableBundle bundle, List<File> versionFolders) {
        if (bundle == null || versionFolders == null) return;

        Map<String, SongFile> songMap = new HashMap<>();

        for (File folder : versionFolders) {
            if (!folder.exists() || !folder.isDirectory()) continue;

            // Determinar TypeFile según la carpeta de versión
            TypeFile typeFile;
            try {
                typeFile = TypeFile.valueOf(folder.getName());
            } catch (IllegalArgumentException e) {
                System.out.println("Carpeta desconocida: " + folder.getName());
                continue;
            }

            // Buscar archivos dentro de la carpeta hasta SEARCH_DEPTH niveles
            List<File> musicFiles = listMusicFiles(folder, SEARCH_DEPTH);

            for (File file : musicFiles) {
                String fileName = file.getName();

                // Filtrar por extensiones válidas
                if (!(fileName.endsWith(".m4a") || fileName.endsWith(".flac"))) continue;

                // Extraer el nombre sin extensión
                String nameWithoutExtension = fileName.contains(".")
                        ? fileName.substring(0, fileName.lastIndexOf('.'))
                        : fileName;

                // Si ya existe la canción, añadir la versión; si no, crear nueva instancia
                SongFile song = songMap.get(nameWithoutExtension);
                if (song == null) {
                    song = new SongFile(nameWithoutExtension, "Desconocido");
                    songMap.put(nameWithoutExtension, song);
                }
                song.addVersion(typeFile);
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

        bundle.table().setItems(songList);
    }

    private static List<File> listMusicFiles(File folder, int maxDepth) {
        List<File> result = new ArrayList<>();
        if (maxDepth < 0) return result;

        File[] files = folder.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isFile()) {
                result.add(file);
            } else if (file.isDirectory()) {
                result.addAll(listMusicFiles(file, maxDepth - 1));
            }
        }

        return result;
    }
}
