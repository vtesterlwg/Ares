package com.playares.arena.match;

import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.timer.cont.MatchEndingTimer;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;

public final class MatchHandler {
    @Getter public final MatchManager manager;

    public MatchHandler(MatchManager manager) {
        this.manager = manager;
    }

    public void finish(Match match) {
        match.getPlugin().getReportManager().getHandler().printReports(match);

        if (match instanceof UnrankedMatch) {
            final UnrankedMatch unrankedMatch = (UnrankedMatch)match;

            // TODO: Print aftermatch report, store reports in match

            if (unrankedMatch.getWinner() != null) {
                if (unrankedMatch.getWinner().equals(unrankedMatch.getPlayerA())) {
                    unrankedMatch.getPlayerA().getActiveReport().pullInventory();
                } else {
                    unrankedMatch.getPlayerB().getActiveReport().pullInventory();
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

            return;
        }
    }
}