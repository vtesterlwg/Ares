package com.playares.lobby;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.commons.bukkit.AresPlugin;
import org.bukkit.event.Listener;

public final class Lobby extends AresPlugin implements Listener {
    @Override
    public void onEnable() {
        final PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");

        registerCommandManager(commandManager);
        registerProtocol(ProtocolLibrary.getProtocolManager());

        registerListener(this);
    }

    @Override
    public void onDisable() {

    }
}