package com.playares.factions.claims.handler;

import com.google.common.collect.Lists;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.claims.ClaimManager;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.claims.pillars.MapPillar;
import com.playares.factions.factions.Faction;
import com.playares.factions.players.FactionPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class ClaimMapHandler {
    @Getter
    public ClaimManager manager;

    public ClaimMapHandler(ClaimManager manager) {
        this.manager = manager;
    }

    public void renderMap(Player viewer, SimplePromise promise) {
        final FactionPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(viewer.getUniqueId());

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        if (!profile.getPillars().isEmpty()) {
            profile.hideAllMapPillars();
            viewer.sendMessage(ChatColor.GRAY + "Hiding existing map pillars...");
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            final List<Faction> found = Lists.newArrayList();
            final PLocatable location = new PLocatable(viewer);

            for (DefinedClaim claim : manager.getClaimRepository()) {
                for (BLocatable corner : claim.getCorners()) {
                    if (corner.distance(location) > 64.0) {
                        continue;
                    }

                    final Faction owner = manager.getPlugin().getFactionManager().getFactionById(claim.getOwnerId());

                    if (owner == null || found.contains(owner)) {
                        continue;
                    }

                    found.add(owner);
                }
            }

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (found.isEmpty()) {
                    promise.failure("No factions found");
                    return;
                }

                for (Faction faction : found) {
                    final List<DefinedClaim> claims = manager.getClaimsByOwner(faction);

                    if (claims.isEmpty()) {
                        continue;
                    }

                    claims.forEach(claim -> {
                        for (BLocatable corner : claim.getCorners()) {
                            final BLocatable adjusted = corner;

                            adjusted.setY(viewer.getLocation().getBlockY() - 5);

                            final MapPillar pillar = new MapPillar(viewer, adjusted, Material.EMERALD_BLOCK);

                            profile.getPillars().add(pillar);

                            pillar.draw();
                        }
                    });
                }
            }).run();
        }).run();
    }
}
