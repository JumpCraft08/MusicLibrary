package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.view.TablePopulator;
import com.jumpcraft08.musiclibrary.model.TableBundle;
import com.jumpcraft08.musiclibrary.model.TypeFile;
import com.jumpcraft08.musiclibrary.util.ConfigManager;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class SelectFolder {

    public enum SelectReason {
        POPULATE_TABLE
    }

    public static void Select(SelectReason reason, TableBundle bundle, TypeFile... typeFile) {
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

        if (baseFolder == null || !baseFolder.isDirectory()) {
            System.out.println("No se ha seleccionado ninguna carpeta");
            return;
        }

        config.setString("lastBaseFolder", baseFolder.getAbsolutePath());

        System.out.println("Carpeta seleccionada: " + baseFolder.getAbsolutePath());

        if (reason == SelectReason.POPULATE_TABLE) {
            Map<TypeFile, File> subFolders = new HashMap<>();

            for (TypeFile type : typeFile) {
                File subFolder = new File(baseFolder, type.name());

                if (subFolder.exists() && subFolder.isDirectory()) {
                    subFolders.put(type, subFolder);
                    System.out.println("Se añadirá a la tabla: " + subFolder.getAbsolutePath());
                } else {
                    System.out.println("No se encontró la carpeta para: " + type.name());
                }
            }

            TablePopulator.populateTable(bundle, subFolders);

        } else {
            System.out.println("Nada que mostrar aqui");
        }
    }
}