package com.playares.arena.player;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager {
    @Getter
    public final Arenas plugin;

    @Getter
    public final Set<ArenaPlayer> players;

    public PlayerManager(Arenas plugin) {
        this.plugin = plugin;
        this.players = Sets.newConcurrentHashSet();
    }

    public ArenaPlayer getPlayer(UUID uniqueId) {
        return players.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public ArenaPlayer getPlayer(String username) {
        return players.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}
