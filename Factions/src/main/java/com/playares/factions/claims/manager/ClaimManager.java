package com.playares.factions.claims.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.location.Locatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.claims.builder.DefinedClaimBuilder;
import com.playares.factions.claims.dao.ClaimDAO;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.handler.ClaimCreationHandler;
import com.playares.factions.claims.handler.ClaimDeleteHandler;
import com.playares.factions.claims.handler.ClaimMapHandler;
import com.playares.factions.claims.world.WorldLocationManager;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.ServerFaction;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClaimManager {
    @Getter public final Factions plugin;
    @Getter public final ClaimCreationHandler creationHandler;
    @Getter public final ClaimDeleteHandler deleteHandler;
    @Getter public final ClaimMapHandler mapHandler;
    @Getter public final WorldLocationManager worldLocationManager;
    @Getter public final Set<DefinedClaim> claimRepository;
    @Getter public final Set<DefinedClaimBuilder> claimBuilders;

    public ClaimManager(Factions plugin) {
        this.plugin = plugin;
        this.creationHandler = new ClaimCreationHandler(this);
        this.deleteHandler = new ClaimDeleteHandler(this);
        this.mapHandler = new ClaimMapHandler(this);
        this.worldLocationManager = new WorldLocationManager(plugin);
        this.claimRepository = Sets.newConcurrentHashSet();
        this.claimBuilders = Sets.newConcurrentHashSet();
    }

    public void loadClaims() {
        claimRepository.addAll(ClaimDAO.getDefinedClaims(plugin, plugin.getMongo()));
        Logger.print("Loaded " + claimRepository.size() + " Claims");
    }

    public void saveClaims(boolean blocking) {
        Logger.print("Saving " + claimRepository.size() + " Claims, Blocking = " + blocking);

        if (blocking) {
            ClaimDAO.saveDefinedClaims(plugin.getMongo(), claimRepository);
            Logger.print("Finished saving claims");
            return;
        }

        new Scheduler(plugin).async(() -> {
            ClaimDAO.saveDefinedClaims(plugin.getMongo(), claimRepository);
            Logger.print("Finished saving claims");
        }).run();
    }

    public DefinedClaim getClaimAt(Locatable location) {
        return claimRepository.stream().filter(claim -> claim.inside(location)).findFirst().orElse(null);
    }

    public DefinedClaim getClaimById(UUID uniqueId) {
        return claimRepository.stream().filter(claim -> claim.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public ImmutableList<DefinedClaim> getClaimsByOwner(Faction faction) {
        return ImmutableList.copyOf(claimRepository.stream().filter(claim -> claim.getOwnerId().equals(faction.getUniqueId())).collect(Collectors.toList()));
    }

    public ImmutableList<DefinedClaim> getClaimsNearby(Locatable location, double distance) {
        return ImmutableList.copyOf(claimRepository.stream().filter(claim -> claim.buffer(location, distance)).collect(Collectors.toList()));
    }

    public ImmutableList<DefinedClaim> getClaimsNearby(Locatable location, boolean buildBuffer) {
        final List<DefinedClaim> result = Lists.newArrayList();

        for (DefinedClaim claim : claimRepository) {
            final Faction faction = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (faction == null) {
                continue;
            }

            if (faction instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)faction;

                if (buildBuffer) {
                    if (claim.buffer(location, sf.getBuildBuffer())) {
                        result.add(claim);
                    }
                } else if (claim.buffer(location, sf.getClaimBuffer())) {
                    result.add(claim);
                }

                continue;
            }

            if (!buildBuffer && claim.buffer(location, plugin.getFactionConfig().getPlayerClaimBuffer())) {
                result.add(claim);
            }
        }

        return ImmutableList.copyOf(result);
    }

    public DefinedClaimBuilder getClaimBuilder(Player player) {
        return claimBuilders.stream().filter(builder -> builder.getClaimer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }
}