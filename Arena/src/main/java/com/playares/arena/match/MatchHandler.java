package com.playares.arena.match;

import com.playares.arena.Arenas;
import com.playares.arena.aftermatch.PlayerReport;
import com.playares.arena.aftermatch.TeamReport;
import com.playares.arena.match.cont.TeamMatch;
import com.playares.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class MatchHandler {
    @Nonnull @Getter
    public Arenas plugin;

    public MatchHandler(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    public void openPlayerReport(@Nonnull Player viewer, @Nonnull UUID playerId, @Nonnull UUID matchId, @Nonnull SimplePromise promise) {
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

    public void openTeamReport(@Nonnull Player viewer, @Nonnull UUID teamId, @Nonnull UUID matchId, @Nonnull SimplePromise promise) {
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
