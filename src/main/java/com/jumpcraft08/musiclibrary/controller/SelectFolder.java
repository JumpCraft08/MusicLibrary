package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.view.TablePopulator;
import com.jumpcraft08.musiclibrary.model.TableBundle;
import com.jumpcraft08.musiclibrary.model.TypeFile;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class SelectFolder {

    public enum SelectReason {
        POPULATE_TABLE
    }

    public static void Select(SelectReason reason, TableBundle bundle, TypeFile... typeFile) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta");

        File baseFolder = directoryChooser.showDialog(new Stage());

        if (baseFolder == null || !baseFolder.isDirectory()) {
            System.out.println("No se ha seleccionado ninguna carpeta");
            return;
        }

        System.out.println("Carpeta seleccionada: " + baseFolder.getAbsolutePath());

        if (reason == SelectReason.POPULATE_TABLE) {
            List<File> subFolders = new ArrayList<>();

            for (TypeFile type : typeFile) {
                File subFolder = new File(baseFolder, type.name());

                if (subFolder.exists() && subFolder.isDirectory()) {
                    subFolders.add(subFolder);
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