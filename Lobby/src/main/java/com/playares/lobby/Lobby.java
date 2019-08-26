package com.playares.lobby;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.lobby.items.ServerSelectorItem;
import com.playares.lobby.listener.PlayerListener;
import com.playares.lobby.queue.QueueManager;
import com.playares.lobby.selector.SelectorManager;
import com.playares.services.customevents.CustomEventService;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.deathban.DeathbanService;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.humbug.HumbugService;
import com.playares.services.profiles.ProfileService;
import com.playares.services.punishments.PunishmentService;
import com.playares.services.ranks.RankService;
import com.playares.services.serversync.ServerSyncService;
import com.playares.services.serversync.data.Server;
import lombok.Getter;

public final class Lobby extends AresPlugin {
    @Getter public LobbyConfig lobbyConfig;
    @Getter public SelectorManager selectorManager;
    @Getter public QueueManager queueManager;

    @Override
    public void onEnable() {
        this.lobbyConfig = new LobbyConfig(this);
        this.selectorManager = new SelectorManager(this);
        this.queueManager = new QueueManager(this);

        lobbyConfig.load();

        // Database
        registerMongo(new MongoDB(lobbyConfig.getDatabaseURI()));
        getMongo().openConnection();

        // Protocol
        registerProtocol(ProtocolLibrary.getProtocolManager());

        // Commands
        final PaperCommandManager commandManager = new PaperCommandManager(this);
        registerCommandManager(commandManager);

        // Services
        registerService(new ProfileService(this));
        registerService(new PunishmentService(this));
        registerService(new HumbugService(this));
        registerService(new EssentialsService(this));
        registerService(new CustomEventService(this));
        registerService(new RankService(this));
        registerService(new CustomItemService(this));
        registerService(new DeathbanService(this));

        registerService(new ServerSyncService(this, new Server(
                this,
                getLobbyConfig().getId(),
                getLobbyConfig().getBungeeName(),
                getLobbyConfig().getDisplayName(),
                getLobbyConfig().getDescription(),
                getLobbyConfig().getType(),
                0)));

        startServices();

        // Listeners
        registerListener(new PlayerListener(this));

        // Bungeecord Messaging Channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Custom Items
        registerCustomItems();
    }

    @Override
    public void onDisable() {
        stopServices();
        getMongo().closeConnection();
    }

    private void registerCustomItems() {
        final CustomItemService customItemService = (CustomItemService)getService(CustomItemService.class);

        if (customItemService == null) {
            Logger.error("Failed to obtain Custom Item Service while attempting to inject custom items");
            return;
        }

        customItemService.registerNewItem(new ServerSelectorItem(this));
    }
}