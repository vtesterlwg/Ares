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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class TeamMatch implements Match {
    @Nonnull @Getter
    public final UUID uniqueId;

    @Getter @Setter
    public long startTimestamp;

    @Nonnull @Getter @Setter
    public MatchStatus status;

    @Nonnull @Getter
    public Mode mode;

    @Nullable @Getter @Setter
    public Arena arena;

    @Nonnull @Getter
    public final Set<Team> opponents;

    @Nonnull @Getter
    public final Set<ArenaPlayer> spectators;

    @Nonnull @Getter
    public final Set<PlayerReport> playerReports;

    @Nonnull @Getter
    public final Set<TeamReport> teamReports;

    public TeamMatch(@Nonnull Team teamA, @Nonnull Team teamB, @Nonnull Mode mode) {
        this.uniqueId = UUID.randomUUID();
        this.status = MatchStatus.COUNTDOWN;
        this.opponents = ImmutableSet.of(teamA, teamB);
        this.mode = mode;
        this.spectators = Sets.newConcurrentHashSet();
        this.playerReports = Sets.newConcurrentHashSet();
        this.teamReports = Sets.newConcurrentHashSet();
    }

    @Nullable
    public ArenaPlayer getPlayer(@Nonnull UUID uniqueId) {
        for (Team team : opponents) {
            for (ArenaPlayer member : team.getMembers()) {
                if (member.getUniqueId().equals(uniqueId)) {
                    return member;
                }
            }
        }

        return null;
    }

    @Nullable
    public Team getTeam(@Nonnull ArenaPlayer player) {
        for (Team team : opponents) {
            if (team.getMembers().contains(player)) {
                return team;
            }
        }

        return null;
    }

    @Nonnull
    public ImmutableCollection<ArenaPlayer> getViewers() {
        final List<ArenaPlayer> result = Lists.newArrayList();
        opponents.forEach(opponent -> result.addAll(opponent.getMembers()));
        result.addAll(spectators);
        return ImmutableList.copyOf(result);
    }

    @Nullable
    public TeamReport getTeamReport(@Nonnull Team team) {
        return teamReports.stream().filter(t -> t.getUniqueId().equals(team.getUniqueId())).findFirst().orElse(null);
    }

    @Nullable
    public TeamReport getTeamReport(@Nonnull UUID uniqueId) {
        return teamReports.stream().filter(t -> t.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    @Nullable
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

    @Nullable
    public Team getLoser(Team winner) {
        for (Team team : opponents) {
            if (!team.getUniqueId().equals(winner.getUniqueId())) {
                return team;
            }
        }

        return null;
    }
}