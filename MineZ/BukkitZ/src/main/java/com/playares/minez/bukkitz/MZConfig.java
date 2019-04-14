package com.playares.minez.bukkitz;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MZConfig {
    @Getter public MineZ plugin;

    public MZConfig(MineZ plugin) {
        this.plugin = plugin;
    }

    @Getter public String databaseURI;
    @Getter public int serverId;
    @Getter public String bungeeName;
    @Getter public boolean PvE;
    @Getter public boolean premiumOnly;

    public void loadValues() {
        final YamlConfiguration config = plugin.getConfig("config");

        this.databaseURI = config.getString("database");
        this.serverId = config.getInt("server-configuration.server-id");
        this.bungeeName = config.getString("server-configuration.bungee-name");
        this.PvE = config.getBoolean("server-configuration.pve");
        this.premiumOnly = config.getBoolean("server-configuration.premium-only");
    }
}