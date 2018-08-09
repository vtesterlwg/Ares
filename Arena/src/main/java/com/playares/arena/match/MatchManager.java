package com.playares.arena.match;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.match.cont.TeamMatch;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MatchManager {
    @Getter
    public final Arenas plugin;

    @Getter
    public final Set<Match> matches;

    public MatchManager(Arenas plugin) {
        this.plugin = plugin;
        this.matches = Sets.newConcurrentHashSet();
    }

    public Match getMatchById(UUID uniqueId) {
        return matches.stream().filter(match -> match.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Match getMatchByTeam(Team team) {
        return matches.stream()
                .filter(match -> match instanceof TeamMatch)
                .filter(teamfight -> ((TeamMatch)teamfight).getOpponents().contains(team))
                .findFirst().orElse(null);
    }

    public Match getMatchBySpectator(ArenaPlayer player) {
        return matches.stream().filter(match -> match.getSpectators().contains(player)).findFirst().orElse(null);
    }

    public ImmutableSet<Match> getMatchByStatus(MatchStatus status) {
        return ImmutableSet.copyOf(matches.stream().filter(match -> match.getStatus().equals(status)).collect(Collectors.toSet()));
    }
}