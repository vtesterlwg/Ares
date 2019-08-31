package com.playares.lobby.queue;

import com.google.common.collect.Maps;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.lobby.Lobby;
import com.playares.services.serversync.data.Server;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

public final class QueueManager {
    @Getter public Lobby lobby;
    @Getter public QueueHandler handler;
    @Getter public final QueueDataListener dataListener;
    @Getter public final Map<Server, ServerQueue> serverQueues;
    @Getter public BukkitTask queueProcessor;
    @Getter public BukkitTask queueNotifier;

    public QueueManager(Lobby lobby) {
        this.lobby = lobby;
        this.handler = new QueueHandler(this);
        this.dataListener = new QueueDataListener(this);
        this.serverQueues = Maps.newConcurrentMap();
        this.queueProcessor = new Scheduler(lobby).sync(() -> handler.processQueues()).repeat(0L, 20L).run();
        this.queueNotifier = new Scheduler(lobby).sync(() -> serverQueues.values().forEach(queue -> queue.getQueue().forEach(queuePlayer -> {
            final Player player = Bukkit.getPlayer(queuePlayer.getUniqueId());
            player.sendMessage(ChatColor.AQUA + "You are currently " + ChatColor.YELLOW + "#" + queue.getPosition(queuePlayer.getUniqueId()) + ChatColor.AQUA + " in queue to join " + queue.getServer().getDisplayName());
        }))).repeat(20L, 10 * 20L).run();

        getLobby().registerListener(dataListener);
    }

    public ServerQueue getQueue(Server server) {
        return serverQueues.get(server);
    }

    public ServerQueue getQueue(Player player) {
        return serverQueues.values().stream().filter(queue -> queue.getPlayer(player.getUniqueId()) != null).findFirst().orElse(null);
    }
}