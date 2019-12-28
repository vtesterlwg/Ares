package com.playares.civilization;

import com.playares.services.serversync.data.Server;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public final class CivConfig {
    @Getter public Civilizations plugin;

    CivConfig(Civilizations plugin) {
        this.plugin = plugin;
    }

    @Getter public String databaseURI;

    // SERVER SYNC
    @Getter public int syncId;
    @Getter public String syncBungeeName;
    @Getter public String syncDisplayName;
    @Getter public String syncDescription;
    @Getter public Server.Type syncType;
    @Getter public int syncPremiumAllocatedSlots;

    // AUTOSAVE
    @Getter public boolean autosaveEnabled;
    @Getter public int autosaveInterval;

    public void load() {
        final YamlConfiguration config = plugin.getConfig("config");

        this.databaseURI = config.getString("database");

        this.syncId = config.getInt("server-data.id");
        this.syncBungeeName = config.getString("server-data.bungee-name");
        this.syncDisplayName = ChatColor.translateAlternateColorCodes('&', config.getString("server-data.display-name"));
        this.syncDescription = ChatColor.translateAlternateColorCodes('&', config.getString("server-data.description"));
        this.syncType = Server.Type.valueOf(config.getString("server-data.type"));
        this.syncPremiumAllocatedSlots = config.getInt("server-data.premium-allocated-slots");
    }
}
