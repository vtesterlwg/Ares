package com.playares.commons.bukkit.timer;

import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class BossTimer {
    @Getter
    public final AresPlugin plugin;

    @Getter
    public final BossBar bossbar;

    @Getter
    public final BossTimerDuration duration;

    @Getter
    public BukkitTask updateTask;

    public BossTimer(AresPlugin plugin, String text, BarColor color, BarStyle style, BossTimerDuration duration) {
        this.plugin = plugin;
        this.bossbar = Bukkit.createBossBar(text, color, style);
        this.duration = duration;
        this.bossbar.setProgress(1.0);
        this.bossbar.setVisible(true);
    }

    public void setText(String text) {
        this.bossbar.setTitle(text);
    }

    public void start() {
        this.updateTask = new Scheduler(plugin).sync(this::tick).repeat(duration.getTickInterval(), duration.getTickInterval()).run();
    }

    private void tick() {
        if ((this.bossbar.getProgress() - 0.1) <= 0.0) {
            this.updateTask.cancel();
            removeAll();
            return;
        }

        this.bossbar.setProgress(this.bossbar.getProgress() - 0.1);
    }

    public void addPlayer(Player player) {
        this.bossbar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        this.bossbar.removePlayer(player);
    }

    public void removeAll() {
        this.bossbar.removeAll();
    }

    @AllArgsConstructor
    public enum BossTimerDuration {
        FIVE_SECONDS(10L),
        TEN_SECONDS(20L),
        FIFTEEN_SECONDS(30L),
        TWENTY_SECONDS(40L),
        THIRTY_SECONDS(50L);

        @Getter
        public final long tickInterval;
    }
}