package com.riotmc.factions.addons.deathbans;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class DeathbanConfig {
    @Getter public final DeathbanAddon addon;
    @Getter @Setter public int normalMinDeathban;
    @Getter @Setter public int normalMaxDeathban;
    @Getter @Setter public int sotwMinDeathban;
    @Getter @Setter public int sotwMaxDeathban;

    public DeathbanConfig(DeathbanAddon addon) {
        this.addon = addon;
    }

    public void loadValues() {
        final YamlConfiguration config = addon.getPlugin().getConfig("config");

        this.normalMinDeathban = config.getInt("deathbans.durations.normal.min");
        this.normalMaxDeathban = config.getInt("deathbans.durations.normal.max");
        this.sotwMinDeathban = config.getInt("deathbans.durations.sotw.min");
        this.sotwMaxDeathban = config.getInt("deathbans.durations.sotw.min");
    }
}
