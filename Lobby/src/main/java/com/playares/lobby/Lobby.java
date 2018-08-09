package com.playares.lobby;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;

public final class Lobby extends AresPlugin {
    @Override
    public void onEnable() {
        final PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");

        registerCommandManager(commandManager);
        registerMongo(new MongoDB("mongodb://localhost"));
        registerProtocol(ProtocolLibrary.getProtocolManager());

        getMongo().openConnection();

        registerService(new CustomEventService(this));
        registerService(new RankService(this));
        registerService(new ProfileService(this));
        registerService(new EssentialsService(this));
        registerService(new HumbugService(this));
        registerService(new PunishmentService(this));
        startServices();
    }

    @Override
    public void onDisable() {

    }
}
