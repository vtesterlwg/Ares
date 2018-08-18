package com.playares.factions.factions;

import com.google.common.collect.Sets;
import com.playares.factions.Factions;
import com.playares.factions.factions.handlers.FactionCreationHandler;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public final class FactionManager {
    @Getter
    public final Factions plugin;

    @Getter
    public final FactionCreationHandler createHandler;

    @Getter
    public final Set<Faction> factionRepository;

    public FactionManager(Factions plugin) {
        this.plugin = plugin;
        this.createHandler = new FactionCreationHandler(this);
        this.factionRepository = Sets.newConcurrentHashSet();
    }

    public Faction getFaction(UUID uniqueId) {
        return factionRepository.stream().filter(f -> f.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Faction getFaction(String name) {
        return factionRepository.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public PlayerFaction getPlayerFaction(UUID uniqueId) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> ((PlayerFaction) pf).isMember(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public ServerFaction getServerFaction(String name) {
        return (ServerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof ServerFaction)
                .filter(sf -> sf.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}