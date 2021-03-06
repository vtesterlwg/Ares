package com.playares.arena.match;

import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import com.playares.arena.timer.cont.MatchEndingTimer;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public final class MatchHandler {
    @Getter public final MatchManager manager;

    public MatchHandler(MatchManager manager) {
        this.manager = manager;
    }

    public void finish(Match match) {
        match.getPlugin().getReportManager().getHandler().printReports(match);

        if (match instanceof UnrankedMatch) {
            final UnrankedMatch unrankedMatch = (UnrankedMatch)match;

            if (unrankedMatch.isRanked()) { // yeah fucked up I get it ok???
                final RankedMatch rankedMatch = (RankedMatch)unrankedMatch;
                final float probabilityA = (1.0F * 1.0F / (1 + 1.0F * (float)(Math.pow(10, 1.0F * (rankedMatch.getRatingB() - rankedMatch.getRatingA()) / 400))));
                final float probabilityB = (1.0F * 1.0F / (1 + 1.0F * (float)(Math.pow(10, 1.0F * (rankedMatch.getRatingA() - rankedMatch.getRatingB()) / 400))));
                int newRatingA;
                int newRatingB;

                if (rankedMatch.getWinner() != null && rankedMatch.getWinner().equals(rankedMatch.getPlayerA())) {
                    newRatingA = (int)(Math.round((rankedMatch.getRatingA() + 30 * (1 - probabilityA)) * 1000000.0 / 1000000.0));
                    newRatingB = (int)(Math.round((rankedMatch.getRatingB() + 30 * (0 - probabilityB)) * 1000000.0 / 1000000.0));
                } else {
                    newRatingA = (int)(Math.round((rankedMatch.getRatingA() + 30 * (0 - probabilityA)) * 1000000.0 / 1000000.0));
                    newRatingB = (int)(Math.round((rankedMatch.getRatingB() + 30 * (1 - probabilityB)) * 1000000.0 / 1000000.0));
                }

                final int diff = Math.abs(newRatingA - rankedMatch.getPlayerA().getRankedData().getRating(rankedMatch.getQueue().getQueueType()));

                rankedMatch.getPlayerA().getRankedData().setRating(rankedMatch.getQueue().getQueueType(), newRatingA);
                rankedMatch.getPlayerB().getRankedData().setRating(rankedMatch.getQueue().getQueueType(), newRatingB);

                if (rankedMatch.getWinner() != null && rankedMatch.getWinner().equals(rankedMatch.getPlayerA())) {
                    match.sendMessage(ChatColor.YELLOW + "Updated Ratings: " + ChatColor.GREEN + rankedMatch.getPlayerA().getUsername() + " +" + diff +
                            " (" + newRatingA + ") " + ChatColor.RED + rankedMatch.getPlayerB().getUsername() + " -" + diff + " (" + newRatingB + ")");
                } else {
                    match.sendMessage(ChatColor.YELLOW + "Updated Ratings: " + ChatColor.GREEN + rankedMatch.getPlayerB().getUsername() + " +" + diff +
                            " (" + newRatingB + ") " + ChatColor.RED + rankedMatch.getPlayerA().getUsername() + " -" + diff + " (" + newRatingA + ")");
                }
            }

            if (unrankedMatch.getWinner() != null) {
                if (unrankedMatch.getWinner().equals(unrankedMatch.getPlayerA())) {
                    unrankedMatch.getPlayerA().getActiveReport().pullInventory();
                    unrankedMatch.getPlayerA().getActiveReport().setHealth(unrankedMatch.getPlayerA().getPlayer().getHealth());
                    unrankedMatch.getPlayerA().getActiveReport().setFood(unrankedMatch.getPlayerA().getPlayer().getFoodLevel());
                } else {
                    unrankedMatch.getPlayerB().getActiveReport().pullInventory();
                    unrankedMatch.getPlayerB().getActiveReport().setHealth(unrankedMatch.getPlayerB().getPlayer().getHealth());
                    unrankedMatch.getPlayerB().getActiveReport().setFood(unrankedMatch.getPlayerB().getPlayer().getFoodLevel());
                }
            }

            manager.getPlugin().getReportManager().addReport(unrankedMatch.getPlayerA().getActiveReport());
            manager.getPlugin().getReportManager().addReport(unrankedMatch.getPlayerB().getActiveReport());

            unrankedMatch.getPlayers().forEach(player -> {
                player.addTimer(new MatchEndingTimer(manager.getPlugin(), player.getUniqueId(), 3));
                player.setStatus(ArenaPlayer.PlayerStatus.SPECTATING);

                Bukkit.getOnlinePlayers().forEach(online -> {
                    online.showPlayer(match.getPlugin(), player.getPlayer());
                    player.getPlayer().showPlayer(match.getPlugin(), online);
                });
            });

            new Scheduler(getManager().getPlugin()).sync(() -> {
                match.getArena().setInUse(false);
                manager.getMatches().remove(match);
            }).delay(3 * 20L).run();
        }

        else if (match instanceof TeamMatch) {
            final TeamMatch teamMatch = (TeamMatch)match;

            if (teamMatch.getWinner() != null) {
                teamMatch.getWinner().getMembers().stream().filter(member -> member.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)).forEach(member ->
                        member.getActiveReport().pullInventory());

                teamMatch.getWinner().getMembers().stream().filter(member -> member.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)).forEach(member -> {
                    member.getActiveReport().pullInventory();
                    member.getActiveReport().setHealth(member.getPlayer().getHealth());
                    member.getActiveReport().setFood(member.getPlayer().getFoodLevel());
                });
            }

            teamMatch.getPlayers().forEach(player -> {
                manager.getPlugin().getReportManager().addReport(player.getActiveReport());

                player.addTimer(new MatchEndingTimer(manager.getPlugin(), player.getUniqueId(), 3));
                player.setStatus(ArenaPlayer.PlayerStatus.SPECTATING);

                Bukkit.getOnlinePlayers().forEach(online -> {
                    online.showPlayer(match.getPlugin(), player.getPlayer());
                    player.getPlayer().showPlayer(match.getPlugin(), online);
                });

                new Scheduler(getManager().getPlugin()).sync(() -> {
                    teamMatch.getTeamA().setStatus(Team.TeamStatus.LOBBY);
                    teamMatch.getTeamB().setStatus(Team.TeamStatus.LOBBY);

                    teamMatch.getArena().setInUse(false);

                    manager.getMatches().remove(match);
                }).delay(3 * 20L).run();
            });
        }
    }
}