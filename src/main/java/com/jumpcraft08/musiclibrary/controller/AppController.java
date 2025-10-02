package com.jumpcraft08.musiclibrary.controller;

import javafx.fxml.FXML;


public class AppController {

    @FXML
    private TableController tableSection;

    @FXML
    public void SelectFolderController() {
        SelectFolder.Select(SelectFolder.SelectReason.POPULATE_TABLE, tableSection);
    }
}
