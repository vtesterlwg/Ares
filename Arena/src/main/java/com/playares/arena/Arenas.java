package com.playares.arena;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.arena.command.TeamCommand;
import com.playares.arena.item.CreateTeamItem;
import com.playares.arena.item.LeaveDisbandTeamItem;
import com.playares.arena.listener.PlayerConnectionListener;
import com.playares.arena.player.PlayerManager;
import com.playares.arena.team.TeamManager;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Arenas extends AresPlugin {
    @Getter public PlayerManager playerManager;
    @Getter public TeamManager teamManager;
    @Getter protected YamlConfiguration configuration;

    @Override
    public void onEnable() {
        this.configuration = getConfig("config");
        this.playerManager = new PlayerManager(this);
        this.teamManager = new TeamManager(this);

        final PaperCommandManager commandManager = new PaperCommandManager(this);
        registerCommandManager(commandManager);

        registerMongo(new MongoDB("mongodb+srv://dev:vIwpVwYNc4WTQkRN@ares-zny4z.mongodb.net/test?retryWrites=true"));
        registerProtocol(ProtocolLibrary.getProtocolManager());

        getMongo().openConnection();

        registerListener(new PlayerConnectionListener(this));

        registerCommand(new TeamCommand(this));

        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new HumbugService(this));
        registerService(new EssentialsService(this));
        registerService(new PunishmentService(this));
        registerService(new ProfileService(this));
        registerService(new RankService(this));
        startServices();

        final CustomItemService customItemService = (CustomItemService)getService(CustomItemService.class);

        if (customItemService != null) {
            customItemService.registerNewItem(new CreateTeamItem(this));
            customItemService.registerNewItem(new LeaveDisbandTeamItem(this));
        } else {
            Logger.error("Failed to obtain Custom Item Service!");
        }
    }

    @Override
    public void onDisable() {
        stopServices();

        if (getMongo() != null) {
            getMongo().closeConnection();
        }
    }
}
