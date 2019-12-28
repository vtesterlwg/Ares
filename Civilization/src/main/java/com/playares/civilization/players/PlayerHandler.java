package com.playares.civilization.players;

import lombok.Getter;
import org.bukkit.entity.Player;

public final class PlayerHandler {
    @Getter public final PlayerManager manager;

    public PlayerHandler(PlayerManager manager) {
        this.manager = manager;
    }

    public void sendTabDisplay(Player player) {

    }
}
