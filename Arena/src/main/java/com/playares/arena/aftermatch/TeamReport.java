package com.playares.arena.aftermatch;

import com.google.common.collect.Maps;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

public final class TeamReport implements AftermatchReport {
    @Getter
    public final UUID uniqueId;

    @Getter
    public final UUID matchId;

    @Getter
    public final String name;

    @Getter
    public final int hits;

    @Getter
    public final double damage;

    @Getter
    public final int usedHealthPotions;

    @Getter
    public final Map<UUID, String> roster;

    public TeamReport(UUID matchId, Team team) {
        this.uniqueId = team.getUniqueId();
        this.matchId = matchId;
        this.name = team.getName();
        this.hits = team.getHits();
        this.damage = team.getDamage();
        this.usedHealthPotions = team.getUsedHealthPotions();
        this.roster = Maps.newHashMap();

        for (ArenaPlayer member : team.getMembers()) {
            roster.put(member.getUniqueId(), member.getUsername());
        }
    }
}