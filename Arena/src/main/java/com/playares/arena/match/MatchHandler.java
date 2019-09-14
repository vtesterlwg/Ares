package com.playares.arena.match;

import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.timer.cont.MatchEndingTimer;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;

public final class MatchHandler {
    @Getter public final MatchManager manager;

    public MatchHandler(MatchManager manager) {
        this.manager = manager;
    }

    public void finish(Match match) {
        if (match instanceof UnrankedMatch) {
            final UnrankedMatch unrankedMatch = (UnrankedMatch)match;

            // TODO: Print aftermatch report, store reports in match

            unrankedMatch.getPlayers().forEach(player -> {
                player.addTimer(new MatchEndingTimer(manager.getPlugin(), player.getUniqueId(), 3));
                player.setStatus(ArenaPlayer.PlayerStatus.SPECTATING);
            });

            new Scheduler(getManager().getPlugin()).sync(() -> {
                unrankedMatch.getPlayers().forEach(player -> match.getArena().setInUse(false));
                manager.getMatches().remove(match);
            }).delay(3 * 20L).run();

            return;
        }
    }
}