package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;
import com.jumpcraft08.file_manager.model.TableBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Clase responsable de poblar la tabla de canciones de la interfaz.
 */
public class TablePopulator {


    public static ObservableList<SongFile> populateTable(TableBundle bundle, List<SongFile> songs) {
        if (bundle == null || songs == null) return FXCollections.observableArrayList();


        List<SongFile> sortedList = new ArrayList<>(songs);
        sortedList.sort(Comparator.comparing(SongFile::getFileName));


        ObservableList<SongFile> observableList = FXCollections.observableArrayList(sortedList);


        bundle.fileNameColumn().setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFileName()));
        bundle.artistColumn().setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArtist()));
        bundle.albumColumn().setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAlbum()));
        bundle.versionsColumn().setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVersionsAsString()));
        bundle.ratingColumn().setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRating()));


        bundle.table().setItems(observableList);
        return observableList;
    }
}
