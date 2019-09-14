package com.playares.arena.match;

import com.destroystokyo.paper.Title;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.team.Team;
import lombok.Getter;

import java.util.List;

public final class TeamMatch extends Match {
    @Getter public final Team teamA;
    @Getter public final Team teamB;

    public TeamMatch(Arenas plugin, MatchmakingQueue queue, Arena arena, boolean ranked, Team teamA, Team teamB) {
        super(plugin, queue, arena, ranked);
        this.teamA = teamA;
        this.teamB = teamB;
    }

    @Override
    public ImmutableList<ArenaPlayer> getPlayers() {
        final List<ArenaPlayer> result = Lists.newArrayList();

        result.addAll(teamA.getAvailableMembers());
        result.addAll(teamB.getAvailableMembers());
        result.addAll(spectators);

        return ImmutableList.copyOf(result);
    }

    public void sendTitle(String header, String footer) {
        getPlayers().forEach(player -> player.getPlayer().sendTitle(new Title(header, footer)));
    }

    public Team getWinner() {
        final int aliveA = (int)teamA.getAvailableMembers().stream().filter(member -> member.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)).count();
        final int aliveB = (int)teamB.getAvailableMembers().stream().filter(member -> member.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)).count();

        if (aliveA == 0 && aliveB == 0) {
            return null;
        }

        if (aliveA > 0 && aliveB == 0) {
            return teamA;
        }

        if (aliveA == 0) {
            return teamB;
        }

        return null;
    }
}