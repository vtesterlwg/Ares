package com.playares.arena.player;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager {
    @Getter public final Arenas plugin;
    @Getter public final PlayerHandler handler;
    @Getter public final Set<ArenaPlayer> players;
    @Getter public BukkitTask playerUpdaterTask;

    public PlayerManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new PlayerHandler(this);
        this.players = Sets.newConcurrentHashSet();
        this.playerUpdaterTask = new Scheduler(plugin).async(() -> players.forEach(ArenaPlayer::update)).repeat(0L, 1L).run();
    }

    public void save(boolean blocking) {
        if (blocking) {
            players.forEach(player -> ArenaPlayerDAO.saveRankedData(plugin.getMongo(), player));
            return;
        }

        new Scheduler(plugin).async(() -> {
            players.forEach(player -> ArenaPlayerDAO.saveRankedData(plugin.getMongo(), player));
        }).run();
    }

    public ArenaPlayer getPlayer(Player bukkitPlayer) {
        return players.stream().filter(player -> player.getUniqueId().equals(bukkitPlayer.getUniqueId())).findFirst().orElse(null);
    }

    public ArenaPlayer getPlayer(UUID uniqueId) {
        return players.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public ArenaPlayer getPlayer(String username) {
        return players.stream().filter(player -> player.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }
}