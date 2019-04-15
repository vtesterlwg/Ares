package com.playares.minez.bukkitz;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MZConfig {
    @Getter public MineZ plugin;

    MZConfig(MineZ plugin) {
        this.plugin = plugin;
    }

    @Getter public String databaseURI;
    @Getter public int serverId;
    @Getter public String bungeeName;
    @Getter public boolean PvE;
    @Getter public boolean premiumOnly;
    @Getter public boolean bleedEnabled;
    @Getter public int bleedInterval;
    @Getter public boolean thirstEnabled;
    @Getter public int thirstInterval;

    void loadValues() {
        final YamlConfiguration config = plugin.getConfig("config");

        this.databaseURI = config.getString("database");
        this.serverId = config.getInt("server-configuration.server-id");
        this.bungeeName = config.getString("server-configuration.bungee-name");
        this.PvE = config.getBoolean("server-configuration.pve");
        this.premiumOnly = config.getBoolean("server-configuration.premium-only");
        this.bleedEnabled = config.getBoolean("mechanics.bleeding.enabled");
        this.bleedInterval = config.getInt("mechanics.bleeding.interval");
        this.thirstEnabled = config.getBoolean("mechanics.thirst.enabled");
        this.thirstInterval = config.getInt("mechanics.thirst.interval");
    }
}