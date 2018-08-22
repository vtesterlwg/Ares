package com.playares.factions.claims;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.location.Locatable;
import com.playares.factions.Factions;
import com.playares.factions.claims.builder.DefinedClaimBuilder;
import com.playares.factions.claims.handler.ClaimCreationHandler;
import com.playares.factions.claims.handler.ClaimMapHandler;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.ServerFaction;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClaimManager {
    @Getter
    public Factions plugin;

    @Getter
    public ClaimCreationHandler creationHandler;

    @Getter
    public ClaimMapHandler mapHandler;

    @Getter
    public Set<DefinedClaim> claimRepository;

    @Getter
    public Set<DefinedClaimBuilder> claimBuilders;

    public ClaimManager(Factions plugin) {
        this.plugin = plugin;
        this.creationHandler = new ClaimCreationHandler(this);
        this.claimRepository = Sets.newConcurrentHashSet();
        this.claimBuilders = Sets.newConcurrentHashSet();
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

    public ImmutableList<DefinedClaim> getClaimsNearby(Locatable location) {
        final List<DefinedClaim> result = Lists.newArrayList();

        for (DefinedClaim claim : claimRepository) {
            final Faction faction = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (faction == null) {
                continue;
            }

            if (faction instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)faction;

                if (claim.buffer(location, sf.getBuffer())) {
                    result.add(claim);
                }

                continue;
            }

            if (claim.buffer(location, plugin.getFactionConfig().getPlayerClaimBuffer())) {
                result.add(claim);
            }
        }

        return ImmutableList.copyOf(result);
    }

    public DefinedClaimBuilder getClaimBuilder(Player player) {
        return claimBuilders.stream().filter(builder -> builder.getClaimer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }
}