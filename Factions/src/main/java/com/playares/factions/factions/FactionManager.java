package com.playares.factions.factions;

import com.google.common.collect.Sets;
import com.playares.commons.base.promise.Promise;
import com.playares.factions.Factions;
import com.playares.factions.factions.handlers.FactionCreationHandler;
import com.playares.services.profiles.ProfileService;
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

    public Faction getFactionById(UUID uniqueId) {
        return factionRepository.stream().filter(f -> f.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Faction getFactionByName(String name) {
        return factionRepository.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public PlayerFaction getPlayerFactionById(UUID uniqueId) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> pf.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionByName(String name) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> pf.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getFactionByPlayer(UUID playerId) {
        return (PlayerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof PlayerFaction)
                .filter(pf -> ((PlayerFaction) pf).isMember(playerId))
                .findFirst()
                .orElse(null);
    }

    public void getFactionByPlayer(String username, Promise<PlayerFaction> promise) {
        final ProfileService profileService = (ProfileService)plugin.getService(ProfileService.class);

        if (profileService == null) {
            promise.ready(null);
            return;
        }

        profileService.getProfile(username, profile -> {
            if (profile == null) {
                promise.ready(null);
                return;
            }

            promise.ready(getFactionByPlayer(profile.getUniqueId()));
        });
    }

    public ServerFaction getServerFactionById(UUID uniqueId) {
        return (ServerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof ServerFaction)
                .filter(sf -> sf.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public ServerFaction getServerFactionByName(String name) {
        return (ServerFaction)factionRepository
                .stream()
                .filter(f -> f instanceof ServerFaction)
                .filter(sf -> sf.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}