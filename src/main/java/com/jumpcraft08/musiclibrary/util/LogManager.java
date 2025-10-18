package com.jumpcraft08.musiclibrary.util;

import java.util.logging.Logger;
import java.util.logging.Level;

public class LogManager {
    private static final Logger GLOBAL_LOGGER = Logger.getLogger("MusicLibrary");

    public static Logger getLogger() {
        return GLOBAL_LOGGER;
    }

    public static void logInfo(Class<?> className, String msg) {
        GLOBAL_LOGGER.logp(Level.INFO, className.getName(), "", msg);
    }

    public static void logError(Class<?> className, String msg, Throwable e) {
        GLOBAL_LOGGER.logp(Level.SEVERE, className.getName(), "", msg, e);
    }
}
