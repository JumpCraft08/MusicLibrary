package com.jumpcraft08.musiclibrary.util;

import java.io.*;

public class ConfigManager {

    private final PropertiesManager manager;

    public ConfigManager() {
        manager = new PropertiesManager("musiclibrary.properties");
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(manager.props.getProperty(key, String.valueOf(defaultValue)));
    }

    public void setBoolean(String key, boolean value) {
        manager.props.setProperty(key, String.valueOf(value));
        manager.save();
    }

    public String getString(String key, String defaultValue) {
        return manager.props.getProperty(key, defaultValue);
    }

    public void setString(String key, String value) {
        manager.props.setProperty(key, value);
        manager.save();
    }
}
