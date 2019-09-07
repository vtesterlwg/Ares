package com.playares.arena.queue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.kit.Kit;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

public final class QueueManager {
    @Getter public final Arenas plugin;
    @Getter public final Set<MatchmakingQueue> matchmakingQueues;
    @Getter public final BukkitTask matchmakerTask;

    public QueueManager(Arenas plugin) {
        this.plugin = plugin;
        this.matchmakingQueues = Sets.newConcurrentHashSet();
        this.matchmakerTask = new Scheduler(getPlugin()).async(() -> {
            if (matchmakingQueues.isEmpty()) {
                return;
            }


        }).repeat(3 * 20L, 3 * 20L).run();
    }

    public MatchmakingQueue getQueue(Player player) {
        return matchmakingQueues.stream().filter(queue -> queue.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    public ImmutableSet<UnrankedQueue> getUnrankedQueues(Kit kit) {
        final Set<UnrankedQueue> results = Sets.newHashSet();
        matchmakingQueues.stream().filter(queue -> queue instanceof UnrankedQueue).filter(unrankedQueue -> unrankedQueue.getKit().equals(kit)).forEach(result -> results.add((UnrankedQueue)result));
        return ImmutableSet.copyOf(results);
    }
}