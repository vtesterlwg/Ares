package com.riotmc.factions.listener;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.claims.DefinedClaim;
import com.riotmc.factions.factions.Faction;
import com.riotmc.factions.factions.ServerFaction;
import com.riotmc.factions.players.FactionPlayer;
import com.riotmc.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

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

    @EventHandler // TODO: Unfinished
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

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

            if (claims.isEmpty() && !profile.getShields().isEmpty()) {
                profile.hideAllShields();
                return;
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
            }
        }).run();
    }
}
