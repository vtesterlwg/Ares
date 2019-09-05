package com.playares.factions.addons.states.listener;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.states.ServerStateAddon;
import com.playares.factions.addons.states.data.ServerState;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.services.serversync.ServerSyncService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.Set;
import java.util.UUID;

public final class EOTWListener implements Listener {
    @Getter public final ServerStateAddon addon;
    @Getter public final Set<UUID> recentlyWarned;

    public EOTWListener(ServerStateAddon addon) {
        this.addon = addon;
        this.recentlyWarned = Sets.newConcurrentHashSet();
    }

    private boolean isRecentlyWarned(UUID uniqueId) {
        return recentlyWarned.contains(uniqueId);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerPortalEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final Location to = event.getTo();

        if (
                !to.getWorld().getEnvironment().equals(World.Environment.NORMAL) &&
                addon.getCurrentState().equals(ServerState.EOTW_PHASE_2) &&
                !addon.isPhase2GracePeriod() &&
                !player.hasPermission("factions.serverstates.bypass")) {

            if (!isRecentlyWarned(uniqueId)) {
                player.sendMessage(ChatColor.RED + "This use of the Nether & End is disabled during " + addon.getCurrentState().getDisplayName());

                recentlyWarned.add(uniqueId);
                new Scheduler(getAddon().getPlugin()).sync(() -> recentlyWarned.remove(uniqueId)).delay(3 * 20L).run();
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = getAddon().getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (!player.hasPermission("factions.serverstates.bypass") && (profile == null || (profile.getStatistics().getTimePlayed() / 1000L) <= 60)
                && (getAddon().getCurrentState().equals(ServerState.EOTW_PHASE_1) || getAddon().getCurrentState().equals(ServerState.EOTW_PHASE_2))) {

            final ServerSyncService serverSyncService = (ServerSyncService)getAddon().getPlugin().getService(ServerSyncService.class);

            if (serverSyncService != null) {
                player.sendMessage(ChatColor.RED + "The final phase of the End of the World has begun. You may play again next map!");
                serverSyncService.sendToLobby(player);
                return;
            } else {
                player.kickPlayer(ChatColor.RED + "The final phase of the End of the World has begun. You may play again next map!");
            }

        }

        if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL) &&
        addon.getCurrentState().equals(ServerState.EOTW_PHASE_2) &&
        !addon.isPhase2GracePeriod() &&
        !player.hasPermission("factions.serverstates.bypass")) {

            player.setHealth(0.0);

        }
    }
}