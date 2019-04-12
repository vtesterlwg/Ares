package com.playares.arena.player;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager {
    @Getter public final Arenas plugin;
    @Getter public final PlayerHandler handler;
    @Getter public final Set<ArenaPlayer> players;

    public PlayerManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new PlayerHandler(this);
        this.players = Sets.newConcurrentHashSet();
    }

    public ArenaPlayer getPlayer(Player bukkitPlayer) {
        return players.stream().filter(player -> player.getUniqueId().equals(bukkitPlayer.getUniqueId())).findFirst().orElse(null);
    }

    public ArenaPlayer getPlayer(UUID uniqueId) {
        return players.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public ArenaPlayer getPlayer(String username) {
        return players.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}