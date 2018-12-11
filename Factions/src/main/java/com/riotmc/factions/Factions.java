package com.riotmc.factions;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.factions.addons.AddonManager;
import com.riotmc.factions.claims.ClaimManager;
import com.riotmc.factions.commands.FactionCommand;
import com.riotmc.factions.commands.PvPCommand;
import com.riotmc.factions.commands.TimerCommand;
import com.riotmc.factions.factions.FactionManager;
import com.riotmc.factions.items.ClaimingStick;
import com.riotmc.factions.listener.*;
import com.riotmc.factions.players.PlayerManager;
import com.riotmc.services.automatedrestarts.AutomatedRestartService;
import com.riotmc.services.classes.ClassService;
import com.riotmc.services.customentity.CustomEntityService;
import com.riotmc.services.customevents.CustomEventService;
import com.riotmc.services.customitems.CustomItemService;
import com.riotmc.services.essentials.EssentialsService;
import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.profiles.ProfileService;
import com.riotmc.services.punishments.PunishmentService;
import com.riotmc.services.ranks.RankService;
import lombok.Getter;

public final class Factions extends RiotPlugin {
    /** Core factions configuration **/
    @Getter protected FactionConfig factionConfig;
    /** Stores all data/handling for Factions **/
    @Getter protected FactionManager factionManager;
    /** Stores all data/handling for Claims **/
    @Getter protected ClaimManager claimManager;
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

        // Commands
        final PaperCommandManager commandManager = new PaperCommandManager(this);
        registerCommandManager(commandManager);
        registerCommand(new FactionCommand(this));
        registerCommand(new TimerCommand(this));
        registerCommand(new PvPCommand(this));

        // Register Services
        registerService(new ClassService(this));
        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new CustomEntityService(this));
        registerService(new EssentialsService(this));
        registerService(new HumbugService(this));
        registerService(new ProfileService(this));
        registerService(new PunishmentService(this));
        registerService(new RankService(this));
        registerService(new AutomatedRestartService(this, 42300));
        startServices();

        // Data Managers
        addonManager = new AddonManager(this);
        factionManager = new FactionManager(this);
        claimManager = new ClaimManager(this);
        playerManager = new PlayerManager(this);

        // Data Loading
        factionManager.loadFactions();
        claimManager.loadClaims();

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

        // Clear Data
        factionManager.getFactionRepository().clear();
        claimManager.getClaimRepository().clear();
        playerManager.getPlayerRepository().clear();

        addonManager.stopAddons();

        // Nullify Classes
        factionManager = null;
        claimManager = null;
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