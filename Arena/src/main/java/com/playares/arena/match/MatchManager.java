package com.playares.arena.match;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public final class MatchManager {
    @Getter public final Arenas plugin;
    @Getter public final MatchHandler handler;
    @Getter public final Set<Match> matches;

    public MatchManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new MatchHandler(this);
        this.matches = Sets.newConcurrentHashSet();
    }

    public Match getMatchById(UUID uniqueId) {
        return matches.stream().filter(match -> match.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Match getMatchByPlayer(ArenaPlayer player) {
        for (Match match : matches) {
            if (match instanceof UnrankedMatch) {
                final UnrankedMatch unrankedMatch = (UnrankedMatch)match;

                if (unrankedMatch.getPlayerA().equals(player) || unrankedMatch.getPlayerB().equals(player)) {
                    return unrankedMatch;
                }
            }

            if (match instanceof TeamMatch) {
                final TeamMatch teamMatch = (TeamMatch)match;

                if (teamMatch.getTeamA().getMembers().contains(player) || teamMatch.getTeamB().getMembers().contains(player)) {
                    return teamMatch;
                }
            }
        }

        return null;
    }
}
