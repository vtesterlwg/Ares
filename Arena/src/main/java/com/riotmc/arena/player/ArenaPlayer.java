package com.riotmc.arena.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ArenaPlayer {
    @Getter public final UUID uniqueId;
    @Getter public final String username;
    @Getter @Setter public PlayerStatus status;

    public ArenaPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.status = PlayerStatus.LOBBY;
    }

    public ArenaPlayer(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.status = PlayerStatus.LOBBY;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public enum PlayerStatus {
        LOBBY, INGAME, INGAME_DEAD, SPECTATING
    }
}