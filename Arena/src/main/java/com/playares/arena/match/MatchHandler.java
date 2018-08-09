package com.playares.arena.match;

import com.playares.arena.Arenas;
import com.playares.arena.aftermatch.PlayerReport;
import com.playares.arena.aftermatch.TeamReport;
import com.playares.arena.match.cont.TeamMatch;
import com.playares.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class MatchHandler {
    @Getter
    public Arenas plugin;

    public MatchHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void openPlayerReport(Player viewer, UUID playerId, UUID matchId, SimplePromise promise) {
        final Match match = plugin.getMatchManager().getMatchById(matchId);

        if (match == null) {
            promise.failure("Match not found");
            return;
        }

        final PlayerReport report = match.getPlayerReport(playerId);

        if (report == null) {
            promise.failure("Player not found");
            return;
        }

        plugin.getMenuHandler().openPlayerReport(viewer, report);
    }

    public void openTeamReport(Player viewer, UUID teamId, UUID matchId, SimplePromise promise) {
        final Match match = plugin.getMatchManager().getMatchById(matchId);

        if (match == null) {
            promise.failure("Match not found");
            return;
        }

        if (!(match instanceof TeamMatch)) {
            promise.failure("Invalid match type");
            return;
        }

        final TeamMatch teamfight = (TeamMatch)match;
        final TeamReport report = teamfight.getTeamReport(teamId);

        if (report == null) {
            promise.failure("Team not found");
            return;
        }

        plugin.getMenuHandler().openTeamReport(viewer, report);
    }
}
