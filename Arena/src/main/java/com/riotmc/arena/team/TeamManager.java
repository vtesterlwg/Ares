package com.riotmc.arena.team;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.player.ArenaPlayer;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class TeamManager {
    @Getter public final Arenas plugin;
    @Getter public final TeamHandler handler;
    @Getter public final Set<Team> teams;

    public TeamManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new TeamHandler(this);
        this.teams = Sets.newConcurrentHashSet();
    }

    public Team getTeam(ArenaPlayer player) {
        return teams.stream().filter(team -> team.getMembers().contains(player)).findFirst().orElse(null);
    }

    public Team getTeam(UUID uniqueId) {
        return teams.stream().filter(team -> team.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Team getTeam(String username) {
        final ArenaPlayer player = plugin.getPlayerManager().getPlayer(username);

        if (player == null) {
            return null;
        }

        return teams.stream().filter(team -> team.getMembers().contains(player)).findFirst().orElse(null);
    }

    public ImmutableList<Team> getAvailableTeams() {
        return ImmutableList.copyOf(teams.stream().filter(team -> team.getStatus().equals(Team.TeamStatus.LOBBY)).collect(Collectors.toList()));
    }
}
