module com.jumpcraft08.musiclibrary {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.logging;
    requires java.desktop;
    requires jaudiotagger;
    requires com.google.gson;
    requires javafx.base;

    opens com.jumpcraft08.musiclibrary to javafx.fxml;
    exports com.jumpcraft08.musiclibrary;

    exports com.jumpcraft08.musiclibrary.controller;
    opens com.jumpcraft08.musiclibrary.controller to javafx.fxml;

    exports com.jumpcraft08.musiclibrary.view;
    opens com.jumpcraft08.musiclibrary.view to javafx.fxml;

    exports com.jumpcraft08.musiclibrary.util;
    exports com.jumpcraft08.musiclibrary.model;

    opens com.jumpcraft08.musiclibrary.model to javafx.fxml, com.google.gson;
    opens com.jumpcraft08.musiclibrary.util to javafx.fxml, com.google.gson;
    exports com.jumpcraft08.flac_implementation.utils;
    opens com.jumpcraft08.flac_implementation.utils to com.google.gson, javafx.fxml;
    exports com.jumpcraft08.flac_implementation.player;
    opens com.jumpcraft08.flac_implementation.player to com.google.gson, javafx.fxml;
    exports com.jumpcraft08.file_manager.model;
    opens com.jumpcraft08.file_manager.model to com.google.gson, javafx.fxml;
    exports com.jumpcraft08.file_manager.util;
    opens com.jumpcraft08.file_manager.util to javafx.fxml;
    exports com.jumpcraft08.file_manager.controller;
    opens com.jumpcraft08.file_manager.controller to com.google.gson, javafx.fxml;
}
