package com.playares.arena.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public final class ArenaUtils {
    public static ImmutableList<Player> getNearbyFriendlies(Arenas plugin, Player player, double range) {
        final List<Player> result = Lists.newArrayList();
        final Team team = plugin.getTeamManager().getTeam(player.getName());

        if (team == null) {
            return ImmutableList.copyOf(result);
        }

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof Player)) {
                continue;
            }

            final Player otherPlayer = (Player)entity;

            if (result.contains(otherPlayer)) {
                continue;
            }

            final ArenaPlayer otherProfile = plugin.getPlayerManager().getPlayer(otherPlayer);

            if (otherProfile == null) {
                continue;
            }

            if (!otherProfile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
                continue;
            }

            if (!team.getMembers().contains(otherProfile)) {
                continue;
            }

            result.add(otherPlayer);
        }

        return ImmutableList.copyOf(result);
    }

    public static ImmutableList<Player> getNearbyEnemies(Arenas plugin, Player player, double range) {
        final List<Player> result = Lists.newArrayList();
        final Team team = plugin.getTeamManager().getTeam(player.getName());

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof Player)) {
                continue;
            }

            final Player otherPlayer = (Player)entity;

            if (result.contains(otherPlayer)) {
                continue;
            }

            final ArenaPlayer otherProfile = plugin.getPlayerManager().getPlayer(otherPlayer);

            if (otherProfile == null) {
                continue;
            }

            if (!otherProfile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
                continue;
            }

            if (team != null && team.getMembers().contains(otherProfile)) {
                continue;
            }

            result.add(otherPlayer);
        }

        return ImmutableList.copyOf(result);
    }
}
