package com.jumpcraft08.musiclibrary.util;

import com.jumpcraft08.musiclibrary.SongFile;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TableBundle {
    private final TableView<SongFile> table;
    private final TableColumn<SongFile, String> fileNameColumn;
    private final TableColumn<SongFile, String> artistColumn;

    public TableBundle(TableView<SongFile> table,
                       TableColumn<SongFile, String> fileNameColumn,
                       TableColumn<SongFile, String> artistColumn) {
        this.table = table;
        this.fileNameColumn = fileNameColumn;
        this.artistColumn = artistColumn;
    }

    public TableView<SongFile> getTable() { return table; }
    public TableColumn<SongFile, String> getFileNameColumn() { return fileNameColumn; }
    public TableColumn<SongFile, String> getArtistColumn() { return artistColumn; }
}
