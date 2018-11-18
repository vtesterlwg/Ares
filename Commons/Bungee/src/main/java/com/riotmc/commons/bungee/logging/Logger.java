package com.riotmc.commons.bungee.logging;

import net.md_5.bungee.api.ProxyServer;

import java.util.logging.Level;

public final class Logger {
    public static void print(String message) {
        ProxyServer.getInstance().getLogger().log(Level.INFO, message);
    }

    public static void warn(String message) {
        ProxyServer.getInstance().getLogger().log(Level.WARNING, message);
    }

    public static void error(String message) {
        ProxyServer.getInstance().getLogger().log(Level.SEVERE, message);
    }

    public static void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }
}
