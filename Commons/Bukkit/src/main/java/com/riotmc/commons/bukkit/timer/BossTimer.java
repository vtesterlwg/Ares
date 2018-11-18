package com.riotmc.commons.bukkit.timer;

import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.util.Scheduler;
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
    public final RiotPlugin plugin;

    @Getter
    public final BossBar bossbar;

    @Getter
    public final BossTimerDuration duration;

    @Getter
    public BukkitTask updateTask;

    public BossTimer(RiotPlugin plugin, String text, BarColor color, BarStyle style, BossTimerDuration duration) {
        this.plugin = plugin;
        this.bossbar = Bukkit.createBossBar(text, color, style);
        this.duration = duration;
        this.bossbar.setProgress(1.0);
        this.bossbar.setVisible(true);
    }

    /**
     * Set the text to display
     * @param text Text
     */
    public void setText(String text) {
        this.bossbar.setTitle(text);
    }

    /**
     * Start the countdown process for this timer
     */
    public void start() {
        this.updateTask = new Scheduler(plugin).sync(this::tick).repeat(duration.getTickInterval(), duration.getTickInterval()).run();
    }

    /**
     * Tick this timer
     */
    private void tick() {
        if ((this.bossbar.getProgress() - 0.1) <= 0.0) {
            this.updateTask.cancel();
            removeAll();
            return;
        }

        this.bossbar.setProgress(this.bossbar.getProgress() - 0.1);
    }

    /**
     * Adds a player to this timer, if added they will see it at the top of their screen
     * @param player Player
     */
    public void addPlayer(Player player) {
        this.bossbar.addPlayer(player);
    }

    /**
     * Removes a player from this timer, if removed they will no longer see it at the top of their screen
     * @param player Player
     */
    public void removePlayer(Player player) {
        this.bossbar.removePlayer(player);
    }

    /**
     * Removes all players from this timer. All players will no longer be able to see it
     */
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