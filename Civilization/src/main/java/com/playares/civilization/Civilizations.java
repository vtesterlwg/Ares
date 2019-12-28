package com.playares.civilization;

import com.playares.civilization.addons.AddonManager;
import com.playares.civilization.players.PlayerManager;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.automatedrestarts.AutomatedRestartService;
import com.playares.services.chatrestrictions.ChatRestrictionService;
import com.playares.services.customentity.CustomEntityService;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;
import com.playares.services.serversync.ServerSyncService;
import com.playares.services.serversync.data.Server;
import com.playares.services.tips.TipService;
import lombok.Getter;

public final class Civilizations extends AresPlugin {
    @Getter public CivConfig civConfig;
    @Getter public PlayerManager playerManager;
    @Getter public AddonManager addonManager;

    @Override
    public void onEnable() {
        civConfig = new CivConfig(this);
        playerManager = new PlayerManager(this);
        addonManager = new AddonManager(this);

        civConfig.load();

        // Register Services
        registerService(new CustomEventService(this));
        registerService(new CustomItemService(this));
        registerService(new CustomEntityService(this));
        registerService(new EssentialsService(this));
        registerService(new HumbugService(this));
        registerService(new ProfileService(this));
        registerService(new PunishmentService(this));
        registerService(new RankService(this));
        registerService(new AutomatedRestartService(this, 42300));
        registerService(new ChatRestrictionService(this));
        registerService(new TipService(this));
        registerService(new ServerSyncService(this, new Server(
                this,
                civConfig.getSyncId(),
                civConfig.getSyncBungeeName(),
                civConfig.getSyncDisplayName(),
                civConfig.getSyncDescription(),
                civConfig.getSyncType(),
                civConfig.getSyncPremiumAllocatedSlots())));

        startServices();

        addonManager.start();

        registerItems();
    }

    @Override
    public void onDisable() {
        playerManager.saveAll();

        addonManager.stop();

        civConfig = null;
        playerManager = null;
        addonManager = null;

        stopServices();
    }

    private void registerItems() {
        final CustomItemService service = (CustomItemService)getService(CustomItemService.class);

        if (service == null) {
            Logger.error("Failed to obtain Custom Item Service");
            return;
        }

        // TODO: Register custom items here
    }
}
