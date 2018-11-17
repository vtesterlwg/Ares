package com.playares.commons.bukkit.service;

import co.aikar.commands.BaseCommand;
import com.playares.commons.bukkit.RiotPlugin;
import org.bukkit.event.Listener;

public interface RiotService {
    /**
     * Start the service
     */
    void start();

    /**
     * Stop the service
     */
    void stop();

    /**
     * @return Returns the name of the Service
     */
    String getName();

    /**
     * @return Returns the owner of this service
     */
    RiotPlugin getOwner();

    /**
     * Registers a listener under the owner of this service
     * @param listener Listener
     */
    default void registerListener(Listener listener) {
        getOwner().registerListener(listener);
    }

    /**
     * Registers a command under the owner of this service
     * @param command Command
     */
    default void registerCommand(BaseCommand command) {
        getOwner().registerCommand(command);
    }
}