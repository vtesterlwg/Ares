package com.playares.services.playerclasses;

import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.service.AresService;
import com.playares.services.playerclasses.listener.ClassListener;
import lombok.Getter;

public final class PlayerClassService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter public ClassManager classManager;

    public PlayerClassService(AresPlugin owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return "Player Classes";
    }

    @Override
    public void start() {
        classManager = new ClassManager(this);
        classManager.load();

        registerListener(new ClassListener(this));
    }

    @Override
    public void stop() {
        classManager.getClasses().clear();
        classManager = null;
    }
}
