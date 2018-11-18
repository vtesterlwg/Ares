package com.riotmc.arena.player;

import com.riotmc.arena.match.Match;
import com.riotmc.arena.scoreboard.ArenaScoreboard;
import com.riotmc.arena.stats.ArcherStatisticHolder;
import com.riotmc.arena.stats.StatisticHolder;
import com.riotmc.arena.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class ArenaPlayer implements StatisticHolder, ArcherStatisticHolder {
    @Nonnull @Getter
    public final UUID uniqueId;

    @Nonnull @Getter
    public final String username;

    @Nullable @Getter @Setter
    public Team team;

    @Nullable @Getter @Setter
    public Match match;

    @Nonnull @Getter @Setter
    public PlayerStatus status;

    @Nullable @Getter @Setter
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

    public ArenaPlayer(@Nonnull Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.team = null;
        this.match = null;
        this.status = PlayerStatus.LOBBY;
        this.scoreboard = null;
    }

    @Nullable
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

        return (double)((arrowHits * 100.0f) / totalArrowsFired);
    }

    public void deleteScoreboard() {
        if (getPlayer() == null) {
            return;
        }

        if (scoreboard == null) {
            getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            return;
        }

        scoreboard.getFriendlyTeam().getEntries().forEach(entry -> scoreboard.getFriendlyTeam().removeEntry(entry));
        scoreboard.getEnemyTeam().getEntries().forEach(entry -> scoreboard.getEnemyTeam().removeEntry(entry));

        getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        setScoreboard(null);
    }
}