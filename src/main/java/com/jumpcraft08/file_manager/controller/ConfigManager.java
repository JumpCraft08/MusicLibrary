package com.jumpcraft08.file_manager.controller;

import com.jumpcraft08.musiclibrary.view.DialogManager;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private final File file;
    private final Properties props = new Properties();

    /** Crea un ConfigManager usando el archivo por defecto "musiclibrary.properties" */
    public ConfigManager() {
        this("musiclibrary.properties");
    }

    /** Crea un ConfigManager usando un archivo de propiedades específico */
    public ConfigManager(String fileName) {
        this.file = new File(fileName);
        load();
    }

    /** Carga el archivo de propiedades si existe */
    private void load() {
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                DialogManager.showError(
                        "Error al cargar propiedades",
                        "No se pudo cargar el archivo de propiedades: " + file.getName(),
                        "Verifica que el archivo exista y tenga permisos de lectura.",
                        e
                );
            }
        }
    }

    /** Guarda el archivo de propiedades */
    private void save() {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Archivo de propiedades: " + file.getName());
        } catch (IOException e) {
            DialogManager.showError(
                    "Error al guardar propiedades",
                    "No se pudo guardar el archivo de propiedades: " + file.getName(),
                    "Verifica que el archivo exista y tenga permisos de escritura.",
                    e
            );
        }
    }

    // --------------------
    // Métodos de acceso
    // --------------------

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(props.getProperty(key, String.valueOf(defaultValue)));
    }

    public void setBoolean(String key, boolean value) {
        props.setProperty(key, String.valueOf(value));
        save();
    }

    public String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public void setString(String key, String value) {
        props.setProperty(key, value);
        save();
    }
}
