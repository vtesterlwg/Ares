package com.playares.arena.arena;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.playares.arena.Arenas;
import com.playares.arena.aftermatch.PlayerReport;
import com.playares.arena.aftermatch.TeamReport;
import com.playares.arena.arena.cont.MainArena;
import com.playares.arena.match.Match;
import com.playares.arena.match.MatchStatus;
import com.playares.arena.match.cont.DuelMatch;
import com.playares.arena.match.cont.TeamMatch;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import com.playares.arena.team.Team;
import com.playares.arena.team.TeamStatus;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.timer.BossTimer;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class ArenaHandler {
    @Getter
    public final Arenas plugin;

    public ArenaHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void startArena(Match match, FailablePromise<Arena> promise) {
        final Arena foundArena = plugin.getArenaManager().getRandomArena();

        if (foundArena == null) {
            promise.failure("Failed to obtain an open Arena");
            return;
        }

        // For now, we will just use MainArenas since we don't plan on any other arena types
        // In the future we will want to perform a check here though
        final MainArena arena = (MainArena)plugin.getArenaManager().getRandomArena();
        final BossTimer timer = new BossTimer(plugin, ChatColor.GOLD + "Match Starting...", BarColor.RED, BarStyle.SEGMENTED_6, BossTimer.BossTimerDuration.FIVE_SECONDS);

        arena.setInUse(true);

        match.setArena(arena);

        if (match instanceof DuelMatch) {
            final DuelMatch duel = (DuelMatch)match;
            final List<ArenaPlayer> opponents = Lists.newArrayList(duel.getOpponents());

            for (int i = 0; i < duel.getOpponents().size(); i++) {
                final PLocatable spawn = arena.getSpawns().get(i);
                final ArenaPlayer opponent = opponents.get(i);

                Players.resetHealth(opponent.getPlayer());
                opponent.getPlayer().setGameMode(GameMode.SURVIVAL);
                opponent.getPlayer().teleport(spawn.getBukkit());
                opponent.setStatus(PlayerStatus.INGAME);
                opponent.setMatch(match);

                timer.addPlayer(opponent.getPlayer());
            }
        }

        if (match instanceof TeamMatch) {
            final TeamMatch teamfight = (TeamMatch)match;
            final List<Team> opponents = Lists.newArrayList(teamfight.getOpponents());

            for (int i = 0; i < teamfight.getOpponents().size(); i++) {
                final PLocatable spawn = arena.getSpawns().get(i);
                final Team opponent = opponents.get(i);

                opponent.teleport(spawn.getBukkit());
                opponent.setStatuses(PlayerStatus.INGAME);
                opponent.getMembers().forEach(member -> {
                    member.setMatch(match);
                    Players.resetHealth(member.getPlayer());
                    member.getPlayer().setGameMode(GameMode.SURVIVAL);
                });

                for (ArenaPlayer player : opponent.getMembers()) {
                    timer.addPlayer(player.getPlayer());
                }
            }
        }

        match.setStatus(MatchStatus.COUNTDOWN);

        timer.start();

        new Scheduler(plugin).sync(() -> {
            match.setStatus(MatchStatus.IN_PROGRESS);
            match.setStartTimestamp(Time.now());

            if (match instanceof DuelMatch) {
                match.sendMessage(((DuelMatch)match).getViewers(), ChatColor.GREEN + "" + ChatColor.BOLD + "Match Started!");
            } else if (match instanceof TeamMatch) {
                match.sendMessage(((TeamMatch)match).getViewers(), ChatColor.GREEN + "" + ChatColor.BOLD + "Match Started!");
            }
        }).delay(5 * 20L).run();

        promise.success(arena);
    }

    public void finishArena(Match match) {
        final Arena arena = match.getArena();

        match.setStatus(MatchStatus.ENDGAME);

        if (match instanceof DuelMatch) {
            final DuelMatch duel = (DuelMatch)match;
            final ArenaPlayer winner = duel.getWinner();

            if (winner != null) {
                final ArenaPlayer loser = duel.getLoser(winner);
                final PlayerReport report = new PlayerReport(winner, match.getUniqueId(), winner.getPlayer().getHealth());
                final Collection<ArenaPlayer> viewers = duel.getViewers();

                winner.setStatus(PlayerStatus.INGAME_DEAD);

                match.getPlayerReports().add(report);
                match.sendTitle(viewers, ChatColor.GOLD + winner.getUsername() + " Wins!", "", 10, 40, 10);

                viewers.forEach(viewer -> plugin.getSpectatorHandler().updateSpectators(viewer));

                if (loser != null) {
                    final List<String> spectatorNames = Lists.newArrayList();
                    match.getSpectators().forEach(spectator -> spectatorNames.add(spectator.getUsername()));

                    match.sendMessage(viewers, " ");
                    match.sendMessage(viewers, ChatColor.AQUA + "Match Report:");

                    final BaseComponent[] aftermatch =
                            new ComponentBuilder("Winner: ")
                            .color(net.md_5.bungee.api.ChatColor.GREEN)
                            .append(winner.getUsername())
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/am p " + winner.getUniqueId().toString() + " " + match.getUniqueId().toString()))
                            .append(" - ")
                            .color(net.md_5.bungee.api.ChatColor.WHITE)
                            .append("Loser: ")
                            .color(net.md_5.bungee.api.ChatColor.RED)
                            .append(loser.getUsername())
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/am p " + loser.getUniqueId().toString() + " " + match.getUniqueId().toString()))
                            .create();

                    match.sendMessage(viewers, aftermatch);

                    if (!match.getSpectators().isEmpty()) {
                        match.sendMessage(viewers, ChatColor.LIGHT_PURPLE + "Spectators: " + ChatColor.GRAY + Joiner.on(ChatColor.GRAY + ", ").join(spectatorNames));
                    }

                    match.sendMessage(viewers, " ");
                }
            }
        }

        if (match instanceof TeamMatch) {
            final TeamMatch teamfight = (TeamMatch)match;
            final Team winner = teamfight.getWinner();
            final Collection<ArenaPlayer> viewers = teamfight.getViewers();

            if (winner != null) {
                final Team loser = teamfight.getLoser(winner);

                winner.getAlive().forEach(alive -> {
                    final PlayerReport report = new PlayerReport(alive, match.getUniqueId(), alive.getPlayer().getHealth());
                    teamfight.getPlayerReports().add(report);

                    alive.setStatus(PlayerStatus.INGAME_DEAD);
                });

                teamfight.getOpponents().forEach(opponent -> {
                    final TeamReport report = new TeamReport(match.getUniqueId(), opponent);
                    teamfight.getTeamReports().add(report);
                });

                match.sendTitle(viewers, ChatColor.GOLD + winner.getName() + " Wins!", "", 10, 40, 10);

                viewers.forEach(viewer -> plugin.getSpectatorHandler().updateSpectators(viewer));

                if (loser != null) {
                    final List<String> spectatorNames = Lists.newArrayList();
                    match.getSpectators().forEach(spectator -> spectatorNames.add(spectator.getUsername()));

                    match.sendMessage(viewers, " ");
                    match.sendMessage(viewers, ChatColor.AQUA + "Match Report:");

                    final BaseComponent[] aftermatch =
                            new ComponentBuilder("Winner: ")
                                    .color(net.md_5.bungee.api.ChatColor.GREEN)
                                    .append(winner.getName())
                                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/am t " + winner.getUniqueId().toString() + " " + match.getUniqueId().toString()))
                                    .append(" - ")
                                    .color(net.md_5.bungee.api.ChatColor.WHITE)
                                    .append("Loser: ")
                                    .color(net.md_5.bungee.api.ChatColor.RED)
                                    .append(loser.getName())
                                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/am t " + loser.getUniqueId().toString() + " " + match.getUniqueId().toString()))
                                    .create();

                    match.sendMessage(viewers, aftermatch);

                    if (!match.getSpectators().isEmpty()) {
                        match.sendMessage(viewers, ChatColor.LIGHT_PURPLE + "Spectators: " + ChatColor.GRAY + Joiner.on(ChatColor.GRAY + ", ").join(spectatorNames));
                    }

                    match.sendMessage(viewers, " ");
                }
            }
        }

        new Scheduler(plugin).sync(() -> {
            final List<ArenaPlayer> viewers = Lists.newArrayList();

            arena.setInUse(false);
            match.setStatus(MatchStatus.FINISHED);

            if (match instanceof DuelMatch) {
                final DuelMatch duel = (DuelMatch)match;
                viewers.addAll(duel.getViewers());
            }

            if (match instanceof TeamMatch) {
                final TeamMatch teamfight = (TeamMatch)match;

                teamfight.getOpponents().forEach(team -> {
                    team.setStatus(TeamStatus.LOBBY);
                    team.resetStats();
                });

                viewers.addAll(teamfight.getViewers());
            }

            viewers.forEach(viewer -> {
                viewer.setStatus(PlayerStatus.LOBBY);
                viewer.setMatch(null);
                viewer.resetStats();

                if (viewer.getPlayer() != null) {
                    plugin.getSpectatorHandler().updateSpectators(viewer);
                    plugin.getPlayerHandler().giveLobbyItems(viewer);
                    viewer.getPlayer().teleport(plugin.getPlayerHandler().getLobby().getBukkit());
                }
            });

            new Scheduler(plugin).sync(() -> plugin.getMatchManager().getMatches().remove(match)).delay(90 * 20L).run();
        }).delay(5 * 20L).run();
    }

    public void createArena(String name, SimplePromise promise) {
        final Arena existing = plugin.getArenaManager().getArena(name);

        if (existing != null) {
            promise.failure("Arena name is already in use");
            return;
        }

        final MainArena arena = new MainArena(name);
        plugin.getArenaManager().getArenas().add(arena);

        Logger.print("Created Arena '" + name + "'");

        promise.success();
    }

    public void setArenaSpawn(Player player, String arenaName, String locationName, SimplePromise promise) {
        final Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            promise.failure("Arena not found");
            return;
        }

        final PLocatable location = new PLocatable(player);

        if (locationName.equalsIgnoreCase("a")) {
            arena.setSpawnA(location);

            plugin.getArenaManager().saveArena(arena);
        } else if (locationName.equalsIgnoreCase("b")) {

            if (arena.getSpawns().isEmpty() || arena.getSpawns().get(0) == null) {
                promise.failure("Spawnpoint A must be set before Spawnpoint B");
                return;
            }

            arena.setSpawnB(location);

            plugin.getArenaManager().saveArena(arena);
        } else {
            promise.failure("Invalid location ID");
            return;
        }

        Logger.print(player.getName() + " updated spawnpoint " + locationName.toUpperCase() + " for arena " + arena.getName());

        promise.success();
    }

    public void setAuthors(Player player, String arenaName, String authors, SimplePromise promise) {
        final Arena arena = plugin.getArenaManager().getArena(arenaName);
        final String[] split = authors.split(", ");

        if (arena == null) {
            promise.failure("Arena not found");
            return;
        }

        arena.getAuthors().clear();
        arena.getAuthors().addAll(Arrays.asList(split));

        plugin.getArenaManager().saveArena(arena);

        Logger.print(player.getName() + " updated authors '" + Joiner.on(", ").join(split) + " for arena " + arena.getName());

        promise.success();
    }

    public void deleteArena(Player player, String arenaName, SimplePromise promise) {
        final Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            promise.failure("Arena not found");
            return;
        }

        if (arena.isInUse()) {
            promise.failure("Can't delete Arena while it is in use");
            return;
        }

        plugin.getArenaManager().getArenas().remove(arena);
        plugin.getArenaManager().deleteArena(arena);

        Logger.print(player.getName() + " deleted arena " + arena.getName());

        promise.success();
    }
}
