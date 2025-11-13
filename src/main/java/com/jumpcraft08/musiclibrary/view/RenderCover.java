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

    // Cache simple de imágenes
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
                    setGraphic(null);
                    imageView.setImage(null);
                } else {
                    setGraphic(imageView);
                    imageView.setImage(null);

                    // Revisar cache
                    String key = coverFile.getAbsolutePath();
                    Image cached = imageCache.get(key);
                    if (cached != null) {
                        imageView.setImage(cached);
                        return;
                    }

                    // Lazy load en background
                    executor.submit(() -> {
                        try {
                            // Cargar thumbnail 64x64 para ahorrar memoria
                            Image img = new Image(coverFile.toURI().toString(), 64, 64, false, false);

                            // Cachear la imagen
                            imageCache.put(key, img);

                            // Actualizar UI en JavaFX Thread
                            javafx.application.Platform.runLater(() -> {
                                if (getItem() == coverFile) {
                                    imageView.setImage(img);
                                }
                            });
                        } catch (Exception ignored) {}
                    });
                }
            }
        };
    }
}
