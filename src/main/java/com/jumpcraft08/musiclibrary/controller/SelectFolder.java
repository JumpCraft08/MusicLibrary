package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.view.TablePopulator;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class SelectFolder {

    public enum SelectReason {
        POPULATE_TABLE
    }

    public static void Select(SelectReason reason, TableController tableController) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta");

        File folder = directoryChooser.showDialog(new Stage());

        if (folder != null && folder.isDirectory()) {
            System.out.println("Carpeta seleccionada: " + folder.getAbsolutePath());
        } else {
            System.out.println("No se ha seleccionado ninguna carpeta");
        }

        if (reason == SelectReason.POPULATE_TABLE) {
            TablePopulator.populateTable(tableController.getTableSong(), folder);
        } else {
            System.out.println("Nada que mostrar");
        }

    }
}