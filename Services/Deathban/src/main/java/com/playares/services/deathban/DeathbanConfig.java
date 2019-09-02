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
    @Getter @Setter public int sotwMaxDeathban;
    @Getter @Setter public int normalMaxDeathban;
    @Getter @Setter public int minDeathbanDuration;
    @Getter @Setter public int lifeUseDelay;

    void loadValues() {
        final YamlConfiguration config = service.getOwner().getConfig("deathbans");

        this.deathbanEnforced = config.getBoolean("enforce-deathbans");
        this.normalMaxDeathban = config.getInt("deathban-durations.NORMAL");
        this.sotwMaxDeathban = config.getInt("deathban-durations.SOTW");
        this.minDeathbanDuration = config.getInt("minimum-deathban-duration");
        this.lifeUseDelay = config.getInt("life-use-delay");
    }
}