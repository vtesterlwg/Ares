package com.playares.factions.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.timer.Timer;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.cont.player.ProtectionTimer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public final class FactionUtils {
    /**
     * Teleports the supplied player outside of enemy claims
     * @param plugin Plugin
     * @param player Player
     */
    public static void teleportOutsideClaims(Factions plugin, Player player) {
        final PLocatable location = new PLocatable(player);

        new Scheduler(plugin).async(() -> {
            while (plugin.getClaimManager().getClaimAt(location) != null) {
                location.setX(location.getX() + 1);
                location.setZ(location.getZ() + 1);
            }

            new Scheduler(plugin).sync(() -> {
                location.setY(location.getBukkit().getWorld().getHighestBlockYAt(location.getBukkit().getBlockX(), location.getBukkit().getBlockZ()));
                player.teleport(location.getBukkit().add(0, 1.0, 0.0));
            }).run();
        }).run();
    }

    /**
     * Returns a list containing all players that are considered friendlies within the supplied distance
     * @param plugin Plugin
     * @param player Player
     * @param distance Distance (radius)
     * @return ImmutableList containing all players considered friendlies
     */
    public static ImmutableList<Player> getNearbyFriendlies(Factions plugin, Player player, double distance) {
        final List<Player> result = Lists.newArrayList();
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        if (faction == null || faction.getMembers().size() <= 1) {
            return ImmutableList.copyOf(result);
        }

        for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
            if (!(entity instanceof Player)) {
                continue;
            }

            final Player otherPlayer = (Player)entity;

            if (otherPlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            if (faction.getMember(otherPlayer.getUniqueId()) != null) {
                result.add(otherPlayer);
            }
        }

        return ImmutableList.copyOf(result);
    }

    /**
     * Returns a list containing all players that are considering enemies within the supplied distance
     * @param plugin Plugin
     * @param player Player
     * @param distance Distance (radius)
     * @return ImmutableList containing all players considered enemies
     */
    public static ImmutableList<Player> getNearbyEnemies(Factions plugin, Player player, double distance) {
        final List<Player> result = Lists.newArrayList();
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
            if (!(entity instanceof Player)) {
                continue;
            }

            final Player otherPlayer = (Player)entity;

            if (!player.canSee(otherPlayer) || otherPlayer.hasPermission("factions.mod") || otherPlayer.hasPermission("factions.admin")) {
                continue;
            }

            if (faction == null) {
                result.add(otherPlayer);
                continue;
            }

            if (faction.getMember(otherPlayer.getUniqueId()) == null) {
                result.add(otherPlayer);
            }
        }

        return ImmutableList.copyOf(result);
    }

    public static void resetPlayer(Factions plugin, Player player) {
        final FactionPlayer factionPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer != null) {
            factionPlayer.getTimers().forEach(Timer::onFinish);
            factionPlayer.getTimers().clear();
            factionPlayer.getTimers().add(new ProtectionTimer(plugin, player.getUniqueId(), plugin.getFactionConfig().getTimerProtection()));
        }

        Players.resetHealth(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }
}