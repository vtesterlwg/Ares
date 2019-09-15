package com.playares.arena.duel;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.team.Team;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;

public final class DuelManager {
    @Getter public final Arenas plugin;
    @Getter public final DuelHandler handler;
    @Getter public final Set<DuelRequest> requests;

    public DuelManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new DuelHandler(this);
        this.requests = Sets.newConcurrentHashSet();
    }

    public void addRequest(DuelRequest request) {
        requests.add(request);
        new Scheduler(getPlugin()).sync(() -> requests.remove(request)).delay(plugin.getArenasConfig().getTimerChallengeExpire() * 20).run();
    }

    public ImmutableSet<PlayerDuelRequest> getPlayerDuelRequests() {
        final Set<PlayerDuelRequest> result = Sets.newHashSet();

        for (DuelRequest request : requests) {
            if (request instanceof PlayerDuelRequest) {
                result.add((PlayerDuelRequest)request);
            }
        }

        return ImmutableSet.copyOf(result);
    }

    public ImmutableSet<TeamDuelRequest> getTeamDuelRequests() {
        final Set<TeamDuelRequest> result = Sets.newHashSet();

        for (DuelRequest request : requests) {
            if (request instanceof TeamDuelRequest) {
                result.add((TeamDuelRequest) request);
            }
        }

        return ImmutableSet.copyOf(result);
    }

    public PlayerDuelRequest getPendingDuelRequest(Player player, String username) {
        return getPlayerDuelRequests()
                .stream()
                .filter(duelRequest -> duelRequest.getRequesting().getUniqueId().equals(player.getUniqueId()) && duelRequest.getRequested().getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public TeamDuelRequest getPendingDuelRequest(Team team, String username) {
        return getTeamDuelRequests()
                .stream()
                .filter(duelRequest -> duelRequest.getRequesting().getUniqueId().equals(team.getUniqueId()) && duelRequest.getRequested().getLeader().getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public PlayerDuelRequest getAcceptedPlayerDuelRequest(Player accepter, String accepting) {
        return getPlayerDuelRequests()
                .stream()
                .filter(duelRequest -> duelRequest.getRequested().getUniqueId().equals(accepter.getUniqueId()) && duelRequest.getRequesting().getUsername().equalsIgnoreCase(accepting))
                .findFirst()
                .orElse(null);
    }

    public TeamDuelRequest getAcceptedTeamDuelRequest(Player accepter, String accepting) {
        final Team team = plugin.getTeamManager().getTeam(accepter.getName());
        final Team acceptingTeam = plugin.getTeamManager().getTeam(accepting);

        if (team == null || acceptingTeam == null) {
            return null;
        }

        return getTeamDuelRequests()
                .stream()
                .filter(teamRequest -> teamRequest.getRequested().getUniqueId().equals(team.getUniqueId()) && teamRequest.getRequesting().getUniqueId().equals(acceptingTeam.getUniqueId()))
                .findFirst()
                .orElse(null);
    }
}