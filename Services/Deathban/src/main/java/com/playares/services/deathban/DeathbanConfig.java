package com.playares.services.deathban;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class DeathbanConfig {
    @Getter public final DeathbanService service;

    DeathbanConfig(DeathbanService service) {
        this.service = service;
    }

    @Getter @Setter public boolean deathbanEnforced;
    @Getter @Setter public int minDeathbanSOTW;
    @Getter @Setter public int maxDeathbanSOTW;
    @Getter @Setter public int minDeathbanNormal;
    @Getter @Setter public int maxDeathbanNormal;
    @Getter @Setter public int lifeUseDelay;

    void loadValues() {
        final YamlConfiguration config = service.getOwner().getConfig("deathbans");

        this.deathbanEnforced = config.getBoolean("settings.enforce-deathbans");
        this.minDeathbanSOTW = config.getInt("settings.deathban-durations.sotw.min");
        this.minDeathbanSOTW = config.getInt("settings.deathban-durations.sotw.max");
        this.minDeathbanSOTW = config.getInt("settings.deathban-durations.normal.min");
        this.minDeathbanSOTW = config.getInt("settings.deathban-durations.normal.min");
        this.lifeUseDelay = config.getInt("settings.life-use-delay");
    }
}
