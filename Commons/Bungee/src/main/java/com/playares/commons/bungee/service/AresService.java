package com.playares.commons.bungee.service;

import co.aikar.commands.BaseCommand;
import com.playares.commons.bungee.AresProxy;
import net.md_5.bungee.api.plugin.Listener;

public interface AresService {
    void start();

    void stop();

    String getName();

    AresProxy getProxy();

    default void registerListener(Listener listener) {
        getProxy().registerListener(listener);
    }

    default void registerCommand(BaseCommand command) {
        getProxy().registerCommand(command);
    }
}
