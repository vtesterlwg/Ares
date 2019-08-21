package com.playares.services.playerclasses;

import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.service.AresService;
import lombok.Getter;

public final class PlayerClassService implements AresService {
    @Getter public final AresPlugin owner;

    public PlayerClassService(AresPlugin owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return "Player Classes";
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
