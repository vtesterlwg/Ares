package com.playares.minez.bukkitz;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.minez.bukkitz.command.MZCommand;
import com.playares.minez.bukkitz.data.manager.ServerManager;
import com.playares.services.customentity.CustomEntityService;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;
import lombok.Getter;

public final class MineZ extends AresPlugin {
    @Getter protected MZConfig MZConfig;
    @Getter public ServerManager serverManager;

    @Override
    public void onEnable() {
        MZConfig = new MZConfig(this);
        MZConfig.loadValues();;

        registerMongo(new MongoDB(MZConfig.getDatabaseURI()));
        getMongo().openConnection();

        registerProtocol(ProtocolLibrary.getProtocolManager());

        final PaperCommandManager commandManager = new PaperCommandManager(this);
        registerCommandManager(commandManager);
        registerCommand(new MZCommand(this));

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new CustomEntityService(this));
        registerService(new EssentialsService(this));
        registerService(new HumbugService(this));
        registerService(new ProfileService(this));
        registerService(new PunishmentService(this));
        registerService(new RankService(this));
        startServices();

        this.serverManager = new ServerManager(this);
    }

    @Override
    public void onDisable() {
        serverManager.closeServer();

        stopServices();
    }
}
