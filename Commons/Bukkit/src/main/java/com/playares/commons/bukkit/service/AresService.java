package com.playares.commons.bukkit.service;

import co.aikar.commands.BaseCommand;
import com.playares.commons.bukkit.AresPlugin;
import org.bukkit.event.Listener;

public interface AresService {
    void start();

    void stop();

    String getName();

    AresPlugin getOwner();

    default void registerListener(Listener listener) {
        getOwner().registerListener(listener);
    }

    default void registerCommand(BaseCommand command) {
        getOwner().registerCommand(command);
    }
}
