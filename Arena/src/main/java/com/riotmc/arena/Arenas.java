package com.riotmc.arena;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.riotmc.arena.command.TeamCommand;
import com.riotmc.arena.item.CreateTeamItem;
import com.riotmc.arena.item.LeaveDisbandTeamItem;
import com.riotmc.arena.listener.PlayerConnectionListener;
import com.riotmc.arena.player.PlayerManager;
import com.riotmc.arena.team.TeamManager;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.services.classes.ClassService;
import com.riotmc.services.customevents.CustomEventService;
import com.riotmc.services.customitems.CustomItemService;
import com.riotmc.services.essentials.EssentialsService;
import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.profiles.ProfileService;
import com.riotmc.services.punishments.PunishmentService;
import com.riotmc.services.ranks.RankService;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Arenas extends RiotPlugin {
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
        registerService(new ClassService(this));
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
