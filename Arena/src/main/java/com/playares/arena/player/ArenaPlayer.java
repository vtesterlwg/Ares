package com.playares.arena.player;

import com.playares.arena.match.Match;
import com.playares.arena.scoreboard.ArenaScoreboard;
import com.playares.arena.stats.ArcherStatisticHolder;
import com.playares.arena.stats.StatisticHolder;
import com.playares.arena.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ArenaPlayer implements StatisticHolder, ArcherStatisticHolder {
    @Getter
    public final UUID uniqueId;

    @Getter
    public final String username;

    @Getter @Setter
    public Team team;

    @Getter @Setter
    public Match match;

    @Getter @Setter
    public PlayerStatus status;

    @Getter @Setter
    public ArenaScoreboard scoreboard;

    @Getter @Setter
    public int hits;

    @Getter @Setter
    public double damage;

    @Getter @Setter
    public double longestShot;

    @Getter @Setter
    public int arrowHits;

    @Getter @Setter
    public int totalArrowsFired;

    public ArenaPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.team = null;
        this.match = null;
        this.status = PlayerStatus.LOBBY;
        this.scoreboard = null;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    @Override
    public void addHit() {
        setHits(getHits() + 1);
    }

    @Override
    public void addDamage(double amount) {
        setDamage(getDamage() + amount);
    }

    public void addArrowHit() {
        setArrowHits(getArrowHits() + 1);
    }

    public void addArrowFired() {
        setTotalArrowsFired(getTotalArrowsFired() + 1);
    }

    public void resetStats() {
        this.hits = 0;
        this.damage = 0.0;
        this.longestShot = 0.0;
        this.arrowHits = 0;
        this.totalArrowsFired = 0;
    }

    @Override
    public double getAccuracy() {
        if (arrowHits == 0 || totalArrowsFired == 0) {
            return 0.0;
        }

        return ((double)(arrowHits / totalArrowsFired) * 100.0);
    }

    public void deleteScoreboard() {
        scoreboard.getFriendlyTeam().getEntries().forEach(entry -> scoreboard.getFriendlyTeam().removeEntry(entry));
        scoreboard.getEnemyTeam().getEntries().forEach(entry -> scoreboard.getEnemyTeam().removeEntry(entry));
        getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        setScoreboard(null);
    }
}