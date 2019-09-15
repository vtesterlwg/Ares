package com.playares.arena.report;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public final class ReportManager {
    @Getter public final Arenas plugin;
    @Getter public final ReportHandler handler;
    @Getter public final Set<Report> reports;

    public ReportManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new ReportHandler(this);
        this.reports = Sets.newConcurrentHashSet();
    }

    public void addReport(Report report) {
        reports.add(report);
        new Scheduler(getPlugin()).sync(() -> reports.remove(report)).delay(300 * 20L).run();
    }

    public Report getReportById(UUID uniqueId) {
        return reports.stream().filter(report -> report.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public PlayerReport getReportByPlayer(ArenaPlayer player) {
        return (PlayerReport)reports.stream().filter(report -> report instanceof PlayerReport).filter(playerReport -> ((PlayerReport) playerReport).getPlayer().equals(player)).findFirst().orElse(null);
    }

    public PlayerReport getReportByPlayer(ArenaPlayer player, UUID matchId) {
        return (PlayerReport)reports
                .stream()
                .filter(report -> report instanceof PlayerReport)
                .filter(playerReport -> ((PlayerReport)playerReport).getPlayer().equals(player))
                .filter(matchReport -> ((PlayerReport) matchReport).getMatchId().equals(matchId))
                .findFirst()
                .orElse(null);
    }
}