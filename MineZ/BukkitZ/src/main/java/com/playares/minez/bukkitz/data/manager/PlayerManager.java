package com.playares.minez.bukkitz.data.manager;

import com.google.common.collect.Sets;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.minez.bukkitz.MineZ;
import com.playares.minez.bukkitz.data.MZPlayer;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager {
    @Getter public final MineZ plugin;
    @Getter public final Set<MZPlayer> players;
    @Getter public BukkitTask updater;

    public PlayerManager(MineZ plugin) {
        this.plugin = plugin;
        this.players = Sets.newConcurrentHashSet();

        this.updater = new Scheduler(plugin).sync(() -> {
            for (MZPlayer player : players) {
                if (plugin.getMZConfig().isThirstEnabled() && player.getNextThirstTick() <= Time.now()) {
                    player.tickThirst();
                }

                if (plugin.getMZConfig().isBleedEnabled() && player.isBleeding() && player.getNextBleedTick() <= Time.now()) {
                    player.tickBleed();
                }
            }
        }).repeat(0L, 1L).run();
    }

    public MZPlayer getLocalPlayer(UUID uniqueId) {
        return players.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public MZPlayer getLocalPlayer(String username) {
        return players.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}
