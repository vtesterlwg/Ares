package com.playares.minez.bukkitz.data.manager;

import com.google.common.collect.Sets;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.minez.bukkitz.MineZ;
import com.playares.minez.bukkitz.data.MZPlayer;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

public final class PlayerManager {
    @Getter public final MineZ plugin;
    @Getter public final Set<MZPlayer> players;
    @Getter public BukkitTask updater;

    public PlayerManager(MineZ plugin) {
        this.plugin = plugin;
        this.players = Sets.newConcurrentHashSet();

        this.updater = new Scheduler(plugin).sync(() -> {
            for (MZPlayer player : players) {
                if (player.getNextThirstTick() <= Time.now()) {
                    player.tickThirst();
                }
            }
        }).repeat(0L, 1L).run();
    }
}
