package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.view.TablePopulator;
import com.jumpcraft08.musiclibrary.SongFile;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.TableView;

import java.io.File;

public class SelectFolder {

    public enum SelectReason {
        POPULATE_TABLE
    }

    public static void Select(SelectReason reason, TableView<SongFile> table) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta");

        File folder = directoryChooser.showDialog(new Stage());

        if (folder != null && folder.isDirectory()) {
            System.out.println("Carpeta seleccionada: " + folder.getAbsolutePath());
        } else {
            System.out.println("No se ha seleccionado ninguna carpeta");
        }

        if (reason == SelectReason.POPULATE_TABLE) {
            TablePopulator.populateTable(table, folder);
        } else {
            System.out.println("Nada que mostrar aqui");
        }

    }
}