package com.playares.factions.listener;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.shields.CombatShield;
import com.playares.factions.claims.shields.ProtectionShield;
import com.playares.factions.claims.shields.Shield;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.factions.data.ServerFaction;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShieldListener implements Listener {
    @Getter
    public final Factions plugin;

    public ShieldListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null) {
            profile.hideAllShields();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null) {
            profile.hideAllShields();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final PLocatable location = new PLocatable(player);

        if (profile == null) {
            return;
        }

        final boolean tagged = profile.hasTimer(PlayerTimer.PlayerTimerType.COMBAT);
        final boolean protection = profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION);

        if (profile.getShields().isEmpty() && !tagged && !protection) {
            return;
        }

        new Scheduler(plugin).async(() -> {
            final List<DefinedClaim> claims = plugin.getClaimManager().getClaimsNearby(new PLocatable(player), 5);

            // No nearby claims, but existing shield blocks from last update must now be removed
            if (claims.isEmpty() && !profile.getShields().isEmpty()) {
                profile.hideAllShields();
                return;
            }

            // Player has existing shields
            // Here we are checking if they are out of reach of old shield blocks and hiding them if so
            if (!profile.getShields().isEmpty()) {
                final Set<Shield> shields = profile.getShields().stream().filter(shield -> shield.getLocation().distance(location) > 5.0).collect(Collectors.toSet());

                if (!shields.isEmpty()) {
                    shields.forEach(Shield::hide);
                    profile.getShields().removeAll(shields);
                }
            }

            if (tagged) {
                final List<DefinedClaim> safezones = Lists.newArrayList();

                for (DefinedClaim claim : claims) {
                    final Faction faction = plugin.getFactionManager().getFactionById(claim.getOwnerId());

                    if (!(faction instanceof ServerFaction)) {
                        continue;
                    }

                    final ServerFaction serverFaction = (ServerFaction)faction;

                    if (!serverFaction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                        continue;
                    }

                    safezones.add(claim);
                }

                if (!safezones.isEmpty()) {
                    final List<BLocatable> perimeters = Lists.newArrayList();

                    for (DefinedClaim claim : safezones) {
                        for (double y = (location.getY() - 1); y < (location.getY() + 5); y++) {
                            final int rounded = (int)Math.round(y);

                            perimeters.addAll(claim.getPerimeter(rounded)
                                    .stream()
                                    .filter(b -> b.distance(location) <= 5.0)
                                    .filter(b ->
                                            b.getBukkit().getType().equals(Material.AIR) ||
                                            b.getBukkit().getType().equals(Material.CAVE_AIR) ||
                                            b.getBukkit().getType().equals(Material.VOID_AIR) ||
                                            b.getBukkit().getType().equals(Material.WATER))
                                    .collect(Collectors.toList()));
                        }
                    }

                    perimeters.stream().filter(shield -> profile.getShieldBlockAt(shield) == null).forEach(shield -> {
                        final CombatShield combatShield = new CombatShield(player, shield);
                        profile.getShields().add(combatShield);
                        combatShield.draw();
                    });
                }
            }

            if (protection) {
                final List<DefinedClaim> combatZones = Lists.newArrayList();

                for (DefinedClaim claim : claims) {
                    final Faction faction = plugin.getFactionManager().getFactionById(claim.getOwnerId());

                    if (faction instanceof PlayerFaction) {
                        combatZones.add(claim);
                        continue;
                    }

                    final ServerFaction serverFaction = (ServerFaction)faction;

                    if (serverFaction.getFlag().equals(ServerFaction.FactionFlag.EVENT)) {
                        combatZones.add(claim);
                    }
                }

                if (!combatZones.isEmpty()) {
                    final List<BLocatable> perimeters = Lists.newArrayList();

                    for (DefinedClaim claim : combatZones) {
                        for (double y = (location.getY() - 1.0); y < (location.getY() + 5); y += 1.0) {
                            final int rounded = (int)Math.round(y);

                            perimeters.addAll(claim.getPerimeter(rounded)
                                    .stream()
                                    .filter(b -> b.distance(location) <= 5.0)
                                    .filter(b ->
                                            b.getBukkit().getType().equals(Material.AIR) ||
                                                    b.getBukkit().getType().equals(Material.CAVE_AIR) ||
                                                    b.getBukkit().getType().equals(Material.VOID_AIR) ||
                                                    b.getBukkit().getType().equals(Material.WATER))
                                    .collect(Collectors.toList()));
                        }
                    }

                    perimeters.stream().filter(shield -> profile.getShieldBlockAt(shield) == null).forEach(shield -> {
                        final ProtectionShield protShield = new ProtectionShield(player, shield);
                        profile.getShields().add(protShield);
                        protShield.draw();
                    });
                }
            }
        }).run();
    }
}
