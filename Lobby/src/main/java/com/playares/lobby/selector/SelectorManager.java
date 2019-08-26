package com.playares.lobby.selector;

import com.playares.lobby.Lobby;
import lombok.Getter;

public final class SelectorManager {
    @Getter public final Lobby plugin;
    @Getter public final SelectorHandler handler;

    public SelectorManager(Lobby plugin) {
        this.plugin = plugin;
        this.handler = new SelectorHandler(this);
    }
}