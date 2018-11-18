package com.riotmc.commons.bukkit.logger;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public final class Logger {
    /**
     * Print to console
     * @param message Message
     */
    public static void print(String message) {
        Bukkit.getLogger().log(Level.INFO, message);
    }

    /**
     * Print a warning to console
     * @param message Warning
     */
    public static void warn(String message) {
        Bukkit.getLogger().log(Level.WARNING, message);
    }

    /**
     * Print an error to console
     * @param message Error
     */
    public static void error(String message) {
        Bukkit.getLogger().log(Level.SEVERE, message);
    }

    /**
     * Print an error with an exception to console
     * @param message Error
     * @param exception Console
     */
    public static void error(String message, Exception exception) {
        error(message);
        exception.printStackTrace();
    }

    private Logger() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}