package com.playares.factions;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.addons.AddonManager;
import com.playares.factions.claims.manager.ClaimManager;
import com.playares.factions.claims.subclaims.manager.SubclaimManager;
import com.playares.factions.commands.FactionCommand;
import com.playares.factions.commands.PvPCommand;
import com.playares.factions.commands.TimerCommand;
import com.playares.factions.factions.manager.FactionManager;
import com.playares.factions.items.ClaimingStick;
import com.playares.factions.listener.*;
import com.playares.factions.players.manager.PlayerManager;
import com.playares.services.automatedrestarts.AutomatedRestartService;
import com.playares.services.chatrestrictions.ChatRestrictionService;
import com.playares.services.customentity.CustomEntityService;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.deathban.DeathbanService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;
import com.playares.services.serversync.ServerSyncService;
import com.playares.services.serversync.data.Server;
import com.playares.services.tips.TipService;
import lombok.Getter;

public final class Factions extends AresPlugin {
    /** Core factions configuration **/
    @Getter protected FactionConfig factionConfig;

    /** Stores all data/handling for Factions **/
    @Getter protected FactionManager factionManager;

    /** Stores all data/handling for Claims **/
    @Getter protected ClaimManager claimManager;

    /** Stores all data/handling for Subclaims **/
    @Getter protected SubclaimManager subclaimManager;

    /** Stores all data/handling for Players **/
    @Getter protected PlayerManager playerManager;

    /** Stores all data/handling for Faction Addons **/
    @Getter protected AddonManager addonManager;

    @Override
    public void onEnable() {
        // Configuration Files
        factionConfig = new FactionConfig(this);
        factionConfig.loadValues();

        // Listeners
        registerListener(new ClaimBuilderListener(this));
        registerListener(new DataListener(this));
        registerListener(new PillarListener(this));
        registerListener(new PlayerTimerListener(this));
        registerListener(new ClaimListener(this));
        registerListener(new PlayerListener(this));
        registerListener(new ChatListener(this));
        registerListener(new CombatListener(this));
        registerListener(new ShieldListener(this));

        // Database
        registerMongo(new MongoDB(factionConfig.getDatabaseURI()));
        getMongo().openConnection();

        // Protocol
        registerProtocol(ProtocolLibrary.getProtocolManager());

        // Bungeecord Messaging Channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Commands
        final PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        registerCommandManager(commandManager);

        registerCommand(new FactionCommand(this));
        registerCommand(new TimerCommand(this));
        registerCommand(new PvPCommand(this));

        // Register Services
        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new CustomEntityService(this));
        registerService(new EssentialsService(this));
        registerService(new HumbugService(this));
        registerService(new ProfileService(this));
        registerService(new PunishmentService(this));
        registerService(new RankService(this));
        registerService(new DeathbanService(this));
        registerService(new AutomatedRestartService(this, 42300));
        registerService(new ChatRestrictionService(this));
        registerService(new PlayerClassService(this));
        registerService(new TipService(this));

        registerService(new ServerSyncService(this, new Server(
                this,
                factionConfig.getSyncId(),
                factionConfig.getSyncBungeeName(),
                factionConfig.getSyncDisplayName(),
                factionConfig.getSyncDescription(),
                factionConfig.getSyncType(),
                factionConfig.getSyncPremiumAllocatedSlots())));

        startServices();

        // Data Managers
        addonManager = new AddonManager(this);
        factionManager = new FactionManager(this);
        claimManager = new ClaimManager(this);
        subclaimManager = new SubclaimManager(this);
        playerManager = new PlayerManager(this);

        // Data Loading
        factionManager.loadFactions();
        claimManager.loadClaims();
        subclaimManager.loadSubclaims();
        claimManager.getWorldLocationManager().load();

        addonManager.startAddons();
        registerItems();
    }

    @Override
    public void onDisable() {
        // Cancel Timers
        factionManager.cancelTasks();
        playerManager.cancelTasks();

        // Data Saving
        playerManager.savePlayers(true);
        factionManager.saveFactions(true);
        claimManager.saveClaims(true);
        subclaimManager.saveSubclaims(true);

        // Clear Data
        factionManager.getFactionRepository().clear();
        claimManager.getClaimRepository().clear();
        playerManager.getPlayerRepository().clear();
        subclaimManager.getSubclaimRepository().clear();

        addonManager.stopAddons();

        // Nullify Classes
        factionManager = null;
        claimManager = null;
        subclaimManager = null;
        playerManager = null;
        addonManager = null;

        stopServices();
    }

    /**
     * Registers custom items used in this Plugin
     */
    private void registerItems() {
        final CustomItemService service = (CustomItemService)getService(CustomItemService.class);

        if (service == null) {
            Logger.error("Failed to obtain Custom Item Service");
            return;
        }

        service.registerNewItem(new ClaimingStick(this));
    }
}