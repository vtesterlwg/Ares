package com.riotmc.commons.bungee.service;

import co.aikar.commands.BaseCommand;
import com.riotmc.commons.bungee.RiotProxy;
import net.md_5.bungee.api.plugin.Listener;

public interface RiotService {
    void start();

    void stop();

    String getName();

    RiotProxy getProxy();

    default void registerListener(Listener listener) {
        getProxy().registerListener(listener);
    }

    default void registerCommand(BaseCommand command) {
        getProxy().registerCommand(command);
    }
}
