package com.jumpcraft08.musiclibrary.controller;

import com.jumpcraft08.musiclibrary.model.Playlist;
import com.jumpcraft08.file_manager.controller.ConfigManager;
import com.jumpcraft08.musiclibrary.util.PlaylistManager;
import com.jumpcraft08.musiclibrary.view.App;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    @FXML private ComboBox<String> PlaybackModeSelector;
    @FXML private ComboBox<String> ThemeSelector;
    @FXML private ComboBox<String> FontSizeSelector;

    @FXML private ListView<String> PlaylistListView;
    @FXML private Button NewPlaylistButton;

    private final ConfigManager config = new ConfigManager();
    private final PlaylistManager playlistManager = new PlaylistManager();

    @FXML
    public void initialize() {
        refreshPlaylistList();
        setupPlaylistContextMenu();

        NewPlaylistButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nueva Playlist");
            dialog.setHeaderText("Ingrese el nombre de la nueva playlist:");
            dialog.setContentText("Nombre:");

            dialog.showAndWait().ifPresent(name -> {
                Playlist newPlaylist = new Playlist(name);
                playlistManager.addPlaylist(newPlaylist);
                refreshPlaylistList();
            });
        });

        // Modos de reproducción
        PlaybackModeSelector.getItems().addAll("No", "Continua", "Playlist");
        PlaybackModeSelector.setValue(config.getString("playbackMode", "No"));
        PlaybackModeSelector.valueProperty().addListener((obs, oldVal, newVal) ->
                config.setString("playbackMode", newVal)
        );

        // Tema
        ThemeSelector.getItems().addAll("Light", "Dark");
        ThemeSelector.setValue(config.getString("theme", "Light"));
        ThemeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            config.setString("theme", newVal);
            applyTheme(newVal);
        });

        // Tamaño de fuente
        FontSizeSelector.getItems().addAll("Pequeña", "Media", "Grande");
        FontSizeSelector.setValue(config.getString("fontSize", "Media"));
        FontSizeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            config.setString("fontSize", newVal);
            applyFontSize(newVal);
        });
    }

    private void refreshPlaylistList() {
        PlaylistListView.getItems().setAll(
                playlistManager.getPlaylists().stream().map(Playlist::getName).toList()
        );
    }

    private void setupPlaylistContextMenu() {
        PlaylistListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            ContextMenu contextMenu = new ContextMenu();
            MenuItem playItem = new MenuItem("Reproducir");
            contextMenu.getItems().add(playItem);

            playItem.setOnAction(e -> {
                int index = cell.getIndex();
                if (index < 0) return;

                Playlist selectedPlaylist = playlistManager.getPlaylists().get(index);
                if (selectedPlaylist.getSongs().isEmpty()) return;

                // Llamar al AppController para reproducir
                App.getMainController().playPlaylist(selectedPlaylist);
            });

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) ->
                    cell.setContextMenu(isNowEmpty ? null : contextMenu)
            );

            return cell;
        });
    }

    private void applyTheme(String theme) {
        System.out.println("Tema cambiado a: " + theme);
    }

    private void applyFontSize(String size) {
        System.out.println("Tamaño de fuente cambiado a: " + size);
    }
}
