package com.jumpcraft08.musiclibrary.util;

import com.jumpcraft08.musiclibrary.view.DialogManager;

import java.io.*;
import java.util.Properties;

public class PropertiesManager {

    private final File file;
    protected final Properties props = new Properties();

    public PropertiesManager(String fileName) {
        this.file = new File(fileName);
        load();
    }

    protected void load() {
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                DialogManager.showError("Error al cargar propiedades", "No se pudo cargar el archivo de propiedades: " + file.getName(), "Verifica que el archivo exista y tenga permisos de lectura.", e);
            }
        }
    }

    protected void save() {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Archivo de propiedades: " + file.getName());
        } catch (IOException e) {
            DialogManager.showError("Error al guardar propiedades", "No se pudo guardar el archivo de propiedades: " + file.getName(), "Verifica que el archivo exista y tenga permisos de escritura.", e);
        }
    }
}
