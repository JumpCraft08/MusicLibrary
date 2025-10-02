package com.jumpcraft08.musiclibrary.util;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "musiclibrary.properties";

    private final Properties props = new Properties();

    public ConfigManager() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("No se pudo cargar config: " + e.getMessage());
            }
        } else {
            System.out.println("No se encontró config.properties, se usará la configuración por defecto.");
        }
        System.out.println("Archivo de configuración: " + new File(CONFIG_FILE).getAbsolutePath());
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(props.getProperty(key, String.valueOf(defaultValue)));
    }

    public void setBoolean(String key, boolean value) {
        props.setProperty(key, String.valueOf(value));
        save();
    }

    private void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuración de MusicLibrary");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
