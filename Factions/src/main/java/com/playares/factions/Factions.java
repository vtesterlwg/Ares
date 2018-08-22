package com.playares.factions;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.claims.ClaimManager;
import com.playares.factions.commands.FactionCommand;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.items.ClaimingStick;
import com.playares.factions.listener.ClaimBuilderListener;
import com.playares.factions.players.PlayerManager;
import com.playares.services.classes.ClassService;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;
import lombok.Getter;

import java.util.List;

public final class Factions extends AresPlugin {
    @Getter
    protected FactionConfig factionConfig;

    @Getter
    protected FactionManager factionManager;

    @Getter
    protected ClaimManager claimManager;

    @Getter
    protected PlayerManager playerManager;

    @Override
    public void onEnable() {
        this.factionConfig = new FactionConfig(this);
        this.factionConfig.loadValues();

        registerMongo(new MongoDB(factionConfig.getDatabaseURI()));
        getMongo().openConnection();

        registerProtocol(ProtocolLibrary.getProtocolManager());

        this.factionManager = new FactionManager(this);
        this.claimManager = new ClaimManager(this);
        this.playerManager = new PlayerManager(this);

        final PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.getCommandCompletions().registerAsyncCompletion("factions", c -> {
            final List<String> names = Lists.newArrayList();
            factionManager.getFactionRepository().forEach(faction -> names.add(faction.getName()));
            return ImmutableList.copyOf(names);
        });

        registerCommandManager(commandManager);

        registerCommand(new FactionCommand(this));

        registerListener(new ClaimBuilderListener(this));

        registerService(new ClassService(this));
        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new EssentialsService(this));
        registerService(new HumbugService(this));
        registerService(new ProfileService(this));
        registerService(new PunishmentService(this));
        registerService(new RankService(this));
        startServices();

        registerItems();
    }

    @Override
    public void onDisable() {
        this.factionManager.getFactionRepository().clear();
        this.claimManager.getClaimRepository().clear();
        this.playerManager.getPlayerRepository().clear();

        this.factionManager = null;
        this.claimManager = null;
        this.playerManager = null;

        stopServices();
    }

    private void registerItems() {
        final CustomItemService service = (CustomItemService)getService(CustomItemService.class);

        if (service == null) {
            Logger.error("Failed to obtain Custom Item Service");
            return;
        }

        service.registerNewItem(new ClaimingStick(this));
    }
}