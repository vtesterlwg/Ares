package com.playares.arena.player;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public final class PlayerManager {
    @Nonnull @Getter
    public final Arenas plugin;

    @Nonnull @Getter
    public final Set<ArenaPlayer> players;

    public PlayerManager(@Nonnull Arenas plugin) {
        this.plugin = plugin;
        this.players = Sets.newConcurrentHashSet();
    }

    @Nullable
    public ArenaPlayer getPlayer(UUID uniqueId) {
        return players.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    @Nullable
    public ArenaPlayer getPlayer(String username) {
        return players.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}