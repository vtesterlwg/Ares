package com.playares.arena.match;

import com.destroystokyo.paper.Title;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.report.PlayerReport;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Set;
import java.util.UUID;

public abstract class Match {
    @Getter public final Arenas plugin;
    @Getter public final UUID uniqueId;
    @Getter public Scoreboard scoreboardA;
    @Getter public Scoreboard scoreboardB;
    @Getter public Scoreboard spectatorScoreboard;
    @Getter public final Set<ArenaPlayer> spectators;
    @Getter public final Set<PlayerReport> playerReports;
    @Getter public final MatchmakingQueue queue;
    @Getter public final Arena arena;
    @Getter public final boolean ranked;

    public Match(Arenas plugin, MatchmakingQueue queue, Arena arena, boolean ranked) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.spectators = Sets.newConcurrentHashSet();
        this.playerReports = Sets.newHashSet();
        this.queue = queue;
        this.arena = arena;
        this.ranked = ranked;
        this.scoreboardA = Bukkit.getScoreboardManager().getNewScoreboard();
        this.scoreboardB = Bukkit.getScoreboardManager().getNewScoreboard();
        this.spectatorScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        setupScoreboards();
    }

    public abstract ImmutableList<ArenaPlayer> getPlayers();

    public void sendMessage(BaseComponent message) {
        getPlayers().forEach(player -> player.getPlayer().sendMessage(message));
    }

    public void sendMessage(String message) {
        getPlayers().forEach(player -> player.getPlayer().sendMessage(message));
    }

    public void sendTitle(String header, String footer) {
        getPlayers().forEach(player -> player.getPlayer().sendTitle(new Title(header, footer)));
    }

    public PlayerReport getReport(ArenaPlayer player) {
        return playerReports.stream().filter(report -> report.getPlayer().equals(player)).findFirst().orElse(null);
    }

    public PlayerReport getReport(UUID playerUUID) {
        return playerReports.stream().filter(report -> report.getPlayer().getUniqueId().equals(playerUUID)).findFirst().orElse(null);
    }

    public void addSpectator(Player player) {
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        getPlayers().forEach(otherPlayer -> {
            if (otherPlayer.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
                otherPlayer.getPlayer().hidePlayer(plugin, player);
            }
        });

        sendMessage(ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " is now spectating");

        spectators.add(profile);
        profile.setStatus(ArenaPlayer.PlayerStatus.SPECTATING);
        arena.teleportToSpectatorSpawnpoint(player);

        addToSpectatorScoreboard(player);

        plugin.getPlayerManager().getHandler().giveItems(profile);
        player.setGameMode(GameMode.CREATIVE);
    }

    public void addToScoreboardA(Player player) {
        if (scoreboardA == null) {
            setupScoreboards();
        }

        final Team friendly = scoreboardA.getTeam("friendly");
        final Team enemy = scoreboardB.getTeam("enemy");
        final Team specTeamA = spectatorScoreboard.getTeam("a");

        friendly.addEntry(player.getName());
        enemy.addEntry(player.getName());
        specTeamA.addEntry(player.getName());

        player.setScoreboard(scoreboardA);
    }

    public void addToScoreboardB(Player player) {
        if (scoreboardB == null) {
            setupScoreboards();
        }

        final Team enemy = scoreboardA.getTeam("enemy");
        final Team friendly = scoreboardB.getTeam("friendly");
        final Team specTeamB = spectatorScoreboard.getTeam("b");

        enemy.addEntry(player.getName());
        friendly.addEntry(player.getName());
        specTeamB.addEntry(player.getName());

        player.setScoreboard(scoreboardB);
    }

    public void addToSpectatorScoreboard(Player player) {
        if (spectatorScoreboard == null) {
            setupScoreboards();
        }

        final Team specA = scoreboardA.getTeam("spectator");
        final Team specB = scoreboardB.getTeam("spectator");
        final Team spectators = spectatorScoreboard.getTeam("spectator");

        specA.addEntry(player.getName());
        specB.addEntry(player.getName());
        spectators.addEntry(player.getName());

        player.setScoreboard(spectatorScoreboard);
    }

    public void removeFromScoreboards(Player player) {
        scoreboardA.getTeams().forEach(team -> {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        });

        scoreboardB.getTeams().forEach(team -> {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        });

        spectatorScoreboard.getTeams().forEach(team -> {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        });
    }

    private void setupScoreboards() {
        final Team friendlyA = scoreboardA.registerNewTeam("friendly");
        final Team enemyA = scoreboardA.registerNewTeam("enemy");
        final Team spectatorA = scoreboardA.registerNewTeam("spectator");

        final Team friendlyB = scoreboardB.registerNewTeam("friendly");
        final Team enemyB = scoreboardB.registerNewTeam("enemy");
        final Team spectatorB = scoreboardB.registerNewTeam("spectator");

        final Team specTeamA = spectatorScoreboard.registerNewTeam("a");
        final Team specTeamB = spectatorScoreboard.registerNewTeam("b");
        final Team spectators = spectatorScoreboard.registerNewTeam("spectator");

        friendlyA.setPrefix(ChatColor.GREEN + "");
        friendlyB.setPrefix(ChatColor.GREEN + "");

        enemyA.setPrefix(ChatColor.RED + "");
        enemyB.setPrefix(ChatColor.RED + "");

        spectatorA.setPrefix(ChatColor.GRAY + "[Spectator]");
        spectatorB.setPrefix(ChatColor.GRAY + "[Spectator]");
        spectators.setPrefix(ChatColor.GRAY + "[Spectator]");

        specTeamA.setPrefix(ChatColor.YELLOW + "");
        specTeamB.setPrefix(ChatColor.AQUA + "");
    }
}
