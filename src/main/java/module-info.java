module com.jumpcraft.musiclibrary {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.jumpcraft08.musiclibrary to javafx.fxml;
    exports com.jumpcraft08.musiclibrary;
    exports com.jumpcraft08.musiclibrary.controller;
    opens com.jumpcraft08.musiclibrary.controller to javafx.fxml;
    exports com.jumpcraft08.musiclibrary.view;
    opens com.jumpcraft08.musiclibrary.view to javafx.fxml;
    exports com.jumpcraft08.musiclibrary.util;
}