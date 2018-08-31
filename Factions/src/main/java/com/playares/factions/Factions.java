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
import com.playares.factions.listener.*;
import com.playares.factions.players.PlayerManager;
import com.playares.factions.util.Autosave;
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

    @Getter
    protected Autosave autosave;

    @Override
    public void onEnable() {
        factionConfig = new FactionConfig(this);
        factionConfig.loadValues();

        registerMongo(new MongoDB(factionConfig.getDatabaseURI()));
        getMongo().openConnection();

        registerProtocol(ProtocolLibrary.getProtocolManager());

        factionManager = new FactionManager(this);
        claimManager = new ClaimManager(this);
        playerManager = new PlayerManager(this);
        autosave = new Autosave(this);

        final PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.getCommandCompletions().registerAsyncCompletion("factions", c -> {
            final List<String> names = Lists.newArrayList();
            factionManager.getFactionRepository().forEach(faction -> names.add(faction.getName()));
            return ImmutableList.copyOf(names);
        });

        registerCommandManager(commandManager);
        registerCommand(new FactionCommand(this));

        factionManager.loadFactions();
        claimManager.loadClaims();

        registerListener(new ClaimBuilderListener(this));
        registerListener(new DataListener(this));
        registerListener(new PillarListener(this));
        registerListener(new PlayerTimerListener(this));
        registerListener(new ClaimListener(this));
        registerListener(new PlayerListener(this));
        registerListener(new ChatListener(this));

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

        if (factionConfig.isAutosaveEnabled()) {
            autosave.start();
        }
    }

    @Override
    public void onDisable() {
        factionManager.cancelTasks();
        playerManager.cancelTasks();

        autosave.stop();

        playerManager.savePlayers(true);
        factionManager.saveFactions(true);
        claimManager.saveClaims(true);

        factionManager.getFactionRepository().clear();
        claimManager.getClaimRepository().clear();
        playerManager.getPlayerRepository().clear();

        factionManager = null;
        claimManager = null;
        playerManager = null;

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