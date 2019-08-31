package com.playares.factions.addons.states.listener;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.states.ServerStateAddon;
import com.playares.factions.addons.states.data.ServerState;
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

        if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL) &&
        addon.getCurrentState().equals(ServerState.EOTW_PHASE_2) &&
        !addon.isPhase2GracePeriod() &&
        !player.hasPermission("factions.serverstates.bypass")) {

            player.setHealth(0.0);

        }
    }
}