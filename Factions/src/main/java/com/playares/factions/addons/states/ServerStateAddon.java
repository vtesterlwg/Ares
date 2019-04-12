package com.playares.factions.addons.states;

import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.states.data.ServerState;
import lombok.Getter;
import lombok.Setter;

public final class ServerStateAddon implements Addon {
    @Getter public final Factions plugin;
    @Getter @Setter public ServerState currentState;

    public ServerStateAddon(Factions plugin) {
        this.plugin = plugin;
        this.currentState = ServerState.NORMAL; // TODO: Make this configurable
    }

    @Override
    public String getName() {
        return "Server States";
    }

    @Override
    public void prepare() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
