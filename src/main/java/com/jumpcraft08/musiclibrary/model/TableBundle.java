package com.jumpcraft08.musiclibrary.model;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public record TableBundle(
        TableView<SongFile> table,
        TableColumn<SongFile, String> fileNameColumn,
        TableColumn<SongFile, String> artistColumn
) {}
