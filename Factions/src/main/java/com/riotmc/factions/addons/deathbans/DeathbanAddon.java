package com.riotmc.factions.addons.deathbans;

import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.Addon;
import com.riotmc.factions.addons.deathbans.command.LivesCommand;
import com.riotmc.factions.addons.deathbans.command.ReviveCommand;
import com.riotmc.factions.addons.deathbans.listener.DeathbanListener;
import com.riotmc.factions.addons.deathbans.manager.DeathbanManager;
import com.riotmc.factions.addons.deathbans.manager.LivesManager;
import lombok.Getter;

public final class DeathbanAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter public DeathbanManager deathbanManager;
    @Getter public LivesManager livesManager;
    @Getter public DeathbanConfig configuration;

    public DeathbanAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Deathbans";
    }

    @Override
    public void prepare() {
        this.configuration = new DeathbanConfig(this);
        this.deathbanManager = new DeathbanManager(this);
        this.livesManager = new LivesManager(this);

        configuration.loadValues();

        plugin.getCommandManager().registerCommand(new LivesCommand(this));
        plugin.getCommandManager().registerCommand(new ReviveCommand(this));

        plugin.registerListener(new DeathbanListener(this));
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}
}
