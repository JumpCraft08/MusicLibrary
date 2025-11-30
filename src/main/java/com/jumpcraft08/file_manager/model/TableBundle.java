package com.jumpcraft08.file_manager.model;

import com.jumpcraft08.musiclibrary.model.SongFile;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public record TableBundle(
        TableView<SongFile> table,
        TableColumn<SongFile, String> fileNameColumn,
        TableColumn<SongFile, String> artistColumn,
        TableColumn<SongFile, String> albumColumn,
        TableColumn<SongFile, String> versionsColumn,
        TableColumn<SongFile, Number> ratingColumn
) {}
