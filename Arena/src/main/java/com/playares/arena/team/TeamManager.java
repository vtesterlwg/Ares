package com.playares.arena.team;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class TeamManager {
    @Getter
    public final Arenas plugin;

    @Getter
    public final Set<Team> teams;

    public TeamManager(Arenas plugin) {
        this.plugin = plugin;
        this.teams = Sets.newConcurrentHashSet();
    }

    public Team getTeam(UUID uniqueId) {
        return teams.stream().filter(team -> team.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Team getTeam(ArenaPlayer player) {
        return teams.stream().filter(team -> team.getMembers().contains(player)).findFirst().orElse(null);
    }

    public List<Team> getAvailableTeams() {
        return teams.stream().filter(team -> team.getStatus().equals(TeamStatus.LOBBY)).collect(Collectors.toList());
    }
}