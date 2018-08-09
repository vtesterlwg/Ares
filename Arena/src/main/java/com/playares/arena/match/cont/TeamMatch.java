package com.playares.arena.match.cont;

import com.google.common.collect.*;
import com.playares.arena.aftermatch.PlayerReport;
import com.playares.arena.aftermatch.TeamReport;
import com.playares.arena.arena.Arena;
import com.playares.arena.match.Match;
import com.playares.arena.match.MatchStatus;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class TeamMatch implements Match {
    @Getter
    public final UUID uniqueId;

    @Getter @Setter
    public long startTimestamp;

    @Getter @Setter
    public MatchStatus status;

    @Getter
    public Mode mode;

    @Getter @Setter
    public Arena arena;

    @Getter
    public final Set<Team> opponents;

    @Getter
    public final Set<ArenaPlayer> spectators;

    @Getter
    public final Set<PlayerReport> playerReports;

    @Getter
    public final Set<TeamReport> teamReports;

    public TeamMatch(Team teamA, Team teamB, Mode mode) {
        this.uniqueId = UUID.randomUUID();
        this.opponents = ImmutableSet.of(teamA, teamB);
        this.mode = mode;
        this.spectators = Sets.newConcurrentHashSet();
        this.playerReports = Sets.newConcurrentHashSet();
        this.teamReports = Sets.newConcurrentHashSet();
    }

    public ArenaPlayer getPlayer(UUID uniqueId) {
        for (Team team : opponents) {
            for (ArenaPlayer member : team.getMembers()) {
                if (member.getUniqueId().equals(uniqueId)) {
                    return member;
                }
            }
        }

        return null;
    }

    public Team getTeam(ArenaPlayer player) {
        for (Team team : opponents) {
            if (team.getMembers().contains(player)) {
                return team;
            }
        }

        return null;
    }

    public ImmutableCollection<ArenaPlayer> getViewers() {
        final List<ArenaPlayer> result = Lists.newArrayList();
        opponents.forEach(opponent -> result.addAll(opponent.getMembers()));
        result.addAll(spectators);
        return ImmutableList.copyOf(result);
    }

    public TeamReport getTeamReport(Team team) {
        return teamReports.stream().filter(t -> t.getUniqueId().equals(team.getUniqueId())).findFirst().orElse(null);
    }

    public TeamReport getTeamReport(UUID uniqueId) {
        return teamReports.stream().filter(t -> t.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Team getWinner() {
        final List<Team> alive = Lists.newArrayList();

        for (Team team : opponents) {
            if (team.getAlive().size() > 0) {
                alive.add(team);
            }
        }

        if (alive.size() != 1) {
            return null;
        }

        return alive.get(0);
    }

    public Team getLoser(Team winner) {
        for (Team team : opponents) {
            if (!team.getUniqueId().equals(winner.getUniqueId())) {
                return team;
            }
        }

        return null;
    }
}