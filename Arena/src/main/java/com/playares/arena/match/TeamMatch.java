package com.playares.arena.match;

import com.destroystokyo.paper.Title;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.kit.Kit;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.report.PlayerReport;
import com.playares.arena.team.Team;
import com.playares.arena.timer.cont.MatchStartingTimer;
import com.playares.commons.bukkit.util.Players;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class TeamMatch extends Match {
    @Getter public final Team teamA;
    @Getter public final Team teamB;

    public TeamMatch(Arenas plugin, MatchmakingQueue queue, Arena arena, Team teamA, Team teamB) {
        super(plugin, queue, arena, false);
        this.teamA = teamA;
        this.teamB = teamB;
    }

    @Override
    public ImmutableList<ArenaPlayer> getPlayers() {
        final List<ArenaPlayer> result = Lists.newArrayList();

        result.addAll(teamA.getAvailableMembers());
        result.addAll(teamB.getAvailableMembers());
        result.addAll(spectators);

        return ImmutableList.copyOf(result);
    }

    public ImmutableList<ArenaPlayer> getAlivePlayers() {
        final List<ArenaPlayer> result = Lists.newArrayList();

        result.addAll(teamA.getAvailableMembers().stream().filter(member -> member.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)).collect(Collectors.toList()));
        result.addAll(teamB.getAvailableMembers().stream().filter(member -> member.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)).collect(Collectors.toList()));

        return ImmutableList.copyOf(result);
    }

    @Override
    public void addSpectator(Player player) {
        super.addSpectator(player);

        getAlivePlayers().forEach(member -> member.getPlayer().hidePlayer(plugin, player));

        player.sendMessage(ChatColor.YELLOW + "You are now spectating " + ChatColor.AQUA + "Team " + teamA.getLeader().getUsername() + ChatColor.YELLOW + " vs. " + ChatColor.AQUA + "Team " + teamB.getLeader().getUsername());
    }

    public void sendTitle(String header, String footer) {
        getPlayers().forEach(player -> player.getPlayer().sendTitle(new Title(header, footer)));
    }

    public void start() {
        getPlayers().forEach(player -> {
            player.addTimer(new MatchStartingTimer(player.getUniqueId(), 5));

            Players.resetHealth(player.getPlayer());
            player.getPlayer().getInventory().clear();
            player.setStatus(ArenaPlayer.PlayerStatus.INGAME);
            player.setActiveReport(new PlayerReport(player));
            player.getActiveReport().setMatchId(getUniqueId());
        });

        getTeamA().getAvailableMembers().forEach(member -> {
            arena.teleportToSpawnpointA(member.getPlayer());
            addToScoreboardA(member.getPlayer());
        });

        getTeamB().getAvailableMembers().forEach(member -> {
            arena.teleportToSpawnpointB(member.getPlayer());
            addToScoreboardB(member.getPlayer());
        });

        teamA.setStatus(Team.TeamStatus.IN_GAME);
        teamB.setStatus(Team.TeamStatus.IN_GAME);

        if (queue.getAllowedKits().isEmpty()) {
            sendMessage(ChatColor.RED + "Failed to find any kits for this queue type");
        } else {
            for (Kit kit : queue.getAllowedKits()) {
                teamA.getAvailableMembers().forEach(member -> member.getPlayer().getInventory().addItem(kit.getBook()));
                teamB.getAvailableMembers().forEach(member -> member.getPlayer().getInventory().addItem(kit.getBook()));
            }
        }

        if (!ranked) {
            sendMessage(ChatColor.YELLOW + "Unranked " + ChatColor.GOLD + getQueue().getQueueType().getDisplayName() + ChatColor.YELLOW + ": " +
                    ChatColor.AQUA + "Team " + teamA.getLeader().getUsername() + ChatColor.YELLOW + " vs. " + ChatColor.AQUA + "Team " + teamB.getLeader().getUsername());
        }
    }

    public Team getWinner() {
        final int aliveA = (int)teamA.getAvailableMembers().stream().filter(member -> member.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)).count();
        final int aliveB = (int)teamB.getAvailableMembers().stream().filter(member -> member.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)).count();

        if (aliveA == 0 && aliveB == 0) {
            return null;
        }

        if (aliveA > 0 && aliveB == 0) {
            return teamA;
        }

        if (aliveA == 0) {
            return teamB;
        }

        return null;
    }
}