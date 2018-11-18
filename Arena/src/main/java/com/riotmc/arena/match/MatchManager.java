package com.riotmc.arena.match;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.match.cont.TeamMatch;
import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.arena.team.Team;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MatchManager {
    @Nonnull @Getter
    public final Arenas plugin;

    @Nonnull @Getter
    public final Set<Match> matches;

    public MatchManager(@Nonnull Arenas plugin) {
        this.plugin = plugin;
        this.matches = Sets.newConcurrentHashSet();
    }

    @Nullable
    public Match getMatchById(@Nonnull UUID uniqueId) {
        return matches.stream().filter(match -> match.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    @Nullable
    public Match getMatchByTeam(@Nonnull Team team) {
        return matches.stream()
                .filter(match -> match instanceof TeamMatch)
                .filter(teamfight -> ((TeamMatch)teamfight).getOpponents().contains(team))
                .findFirst().orElse(null);
    }

    @Nullable
    public Match getMatchBySpectator(@Nonnull ArenaPlayer player) {
        return matches.stream().filter(match -> match.getSpectators().contains(player)).findFirst().orElse(null);
    }

    @Nullable
    public ImmutableSet<Match> getMatchByStatus(@Nonnull MatchStatus status) {
        return ImmutableSet.copyOf(matches.stream().filter(match -> match.getStatus().equals(status)).collect(Collectors.toSet()));
    }
}