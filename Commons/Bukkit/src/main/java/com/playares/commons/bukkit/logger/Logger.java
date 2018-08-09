package com.playares.commons.bukkit.logger;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public final class Logger {
    public static void print(String message) {
        Bukkit.getLogger().log(Level.INFO, message);
    }

    public static void warn(String message) {
        Bukkit.getLogger().log(Level.WARNING, message);
    }

    public static void error(String message) {
        Bukkit.getLogger().log(Level.SEVERE, message);
    }

    public static void error(String message, Exception exception) {
        error(message);
        exception.printStackTrace();
    }

    private Logger() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}