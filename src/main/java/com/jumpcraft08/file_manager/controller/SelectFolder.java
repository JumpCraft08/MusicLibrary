package com.jumpcraft08.file_manager.controller;

import com.jumpcraft08.file_manager.model.TableBundle;
import com.jumpcraft08.musiclibrary.model.TypeFile;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class SelectFolder {

    public static Map<TypeFile, File> select(TableBundle bundle, TypeFile... typeFile) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta");

        ConfigManager config = new ConfigManager();
        String lastFolder = config.getString("lastBaseFolder", null);
        if (lastFolder != null) {
            File lastFolderFile = new File(lastFolder);
            if (lastFolderFile.exists() && lastFolderFile.isDirectory()) {
                directoryChooser.setInitialDirectory(lastFolderFile);
            }
        }

        File baseFolder = directoryChooser.showDialog(new Stage());
        if (baseFolder == null || !baseFolder.isDirectory()) return null;

        config.setString("lastBaseFolder", baseFolder.getAbsolutePath());

        Map<TypeFile, File> subFolders = new HashMap<>();
        for (TypeFile type : typeFile) {
            File subFolder = new File(baseFolder, type.name());
            if (subFolder.exists() && subFolder.isDirectory()) {
                subFolders.put(type, subFolder);
            }
        }
        return subFolders;
    }
}