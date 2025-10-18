package com.jumpcraft08.musiclibrary.util;

import com.jumpcraft08.musiclibrary.model.SongFile;

import javafx.scene.control.TableColumn;

import java.io.*;

public class RatingManager {

    private final PropertiesManager manager;

    public RatingManager() {
        manager = new PropertiesManager("ratings.properties");
    }

    public int getRating(String songName) {
        String val = manager.props.getProperty(songName);
        if (val == null) return -1;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void setRating(String songName, int rating) {
        manager.props.setProperty(songName, String.valueOf(rating));
        manager.save();
    }

    public void configureRatingColumn(TableColumn<SongFile, Number> ratingColumn) {
        ratingColumn.setCellValueFactory(cell -> cell.getValue().ratingProperty());
        ratingColumn.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Number rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null || rating.intValue() < 0) {
                    setText("");
                } else {
                    setText(String.valueOf(rating.intValue()));
                }
            }
        });
    }
}
