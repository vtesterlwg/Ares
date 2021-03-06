package com.playares.factions.claims.handler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.manager.ClaimManager;
import com.playares.factions.claims.pillars.MapPillar;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.players.data.FactionPlayer;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
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
        final ImmutableList<Material> pillarMaterials = ImmutableList.of(
                Material.BOOKSHELF, Material.SOUL_SAND, Material.PUMPKIN, Material.SPONGE, Material.EMERALD_BLOCK,
                Material.DIAMOND_BLOCK, Material.REDSTONE_BLOCK, Material.SANDSTONE, Material.LAPIS_BLOCK,
                Material.IRON_BLOCK, Material.PRISMARINE, Material.PURPUR_BLOCK, Material.NETHERRACK,
                Material.RED_NETHER_BRICK, Material.MELON, Material.ENDER_STONE, Material.QUARTZ_BLOCK);

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        if (profile.hasMapPillars()) {
            profile.hideAllMapPillars();
            viewer.sendMessage(ChatColor.GRAY + "Hiding existing map pillars...");
            promise.success();
            return;
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

                viewer.sendMessage(ChatColor.BLUE + "Faction Map" + ChatColor.YELLOW + " (" + found.size() + " in your area)");

                int pos = 0;

                for (Faction faction : found) {
                    final List<DefinedClaim> claims = manager.getClaimsByOwner(faction);

                    if (claims.isEmpty()) {
                        continue;
                    }

                    final Material pillarMat = pillarMaterials.get(pos);

                    claims.forEach(claim -> {
                        for (BLocatable corner : claim.getCorners()) {
                            final BLocatable adjusted = corner;

                            adjusted.setY(viewer.getLocation().getBlockY() - 5);

                            final MapPillar pillar = new MapPillar(viewer, adjusted, pillarMat);

                            profile.getPillars().add(pillar);

                            pillar.draw();
                        }
                    });

                    viewer.sendMessage(ChatColor.GOLD + faction.getName() + ChatColor.YELLOW + " - " + ChatColor.WHITE + StringUtils.capitaliseAllWords(pillarMat.name().toLowerCase().replace("_", " ")));

                    pos++;
                }
            }).run();
        }).run();
    }
}
