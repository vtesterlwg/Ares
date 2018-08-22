package com.playares.factions.players;

import com.google.common.collect.Sets;
import com.playares.factions.Factions;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager {
    @Getter
    public final Factions plugin;

    @Getter
    public final Set<FactionPlayer> playerRepository;

    public PlayerManager(Factions plugin) {
        this.plugin = plugin;
        this.playerRepository = Sets.newConcurrentHashSet();
    }

    public FactionPlayer getPlayer(UUID uniqueId) {
        return playerRepository.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public FactionPlayer getPlayer(String username) {
        return playerRepository.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}
