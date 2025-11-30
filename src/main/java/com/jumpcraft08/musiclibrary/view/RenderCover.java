package com.jumpcraft08.musiclibrary.view;

import com.jumpcraft08.musiclibrary.model.SongFile;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

public class RenderCover {

    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static Callback<TableColumn<SongFile, File>, TableCell<SongFile, File>> create() {
        return col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(64);
                imageView.setFitHeight(64);
                imageView.setPreserveRatio(false);
            }

            @Override
            protected void updateItem(File coverFile, boolean empty) {
                super.updateItem(coverFile, empty);

                if (empty || coverFile == null || !coverFile.exists()) {
                    clearImage();
                } else {
                    setGraphic(imageView);
                    loadImage(coverFile);
                }
            }

            private void clearImage() {
                setGraphic(null);
                imageView.setImage(null);
            }

            private void loadImage(File coverFile) {
                imageView.setImage(null);
                String key = coverFile.getAbsolutePath();
                Image cached = imageCache.get(key);
                if (cached != null) {
                    imageView.setImage(cached);
                    return;
                }

                executor.submit(() -> {
                    try {
                        Image img = new Image(coverFile.toURI().toString(), 64, 64, false, false);
                        imageCache.put(key, img);
                        javafx.application.Platform.runLater(() -> {
                            if (getItem() == coverFile) {
                                imageView.setImage(img);
                            }
                        });
                    } catch (Exception ignored) {}
                });
            }
        };
    }
}
