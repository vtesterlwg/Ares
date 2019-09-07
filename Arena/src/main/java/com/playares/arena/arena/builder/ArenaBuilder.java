package com.playares.arena.arena.builder;

import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.commons.bukkit.location.PLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ArenaBuilder {
    @Getter public final Arenas plugin;
    @Getter public final UUID uniqueId;
    @Getter public final String name;
    @Getter public final String displayName;
    @Getter public PLocatable spectatorSpawn;
    @Getter public PLocatable spawnA;
    @Getter public PLocatable spawnB;
    @Getter @Setter public ArenaBuilderState currentState;

    ArenaBuilder(Arenas plugin, UUID uniqueId, String name, String displayName) {
        this.plugin = plugin;
        this.uniqueId = uniqueId;
        this.name = name;
        this.displayName = displayName;
        this.currentState = ArenaBuilderState.SET_SPEC;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    void setSpectatorSpawn(PLocatable location) {
        this.spectatorSpawn = location;
        getPlayer().sendMessage(ChatColor.GREEN + "Arena spectator spawn has been set");
        setCurrentState(ArenaBuilderState.SET_A);
        getPlayer().sendMessage(ChatColor.YELLOW + "Stand at the first player spawnpoint and type /arena set");
    }

    void setSpawnA(PLocatable location) {
        this.spawnA = location;
        getPlayer().sendMessage(ChatColor.GREEN + "Arena spawn 'A' has been set");
        setCurrentState(ArenaBuilderState.SET_B);
        getPlayer().sendMessage(ChatColor.YELLOW + "Stand at the second player spawnpoint and type /arena set");
    }

    void setSpawnB(PLocatable location) {
        this.spawnB = location;
        getPlayer().sendMessage(ChatColor.GREEN + "Arena spawn 'A' has been set");
        setCurrentState(ArenaBuilderState.SET_B);
    }

    Arena build() {
        return new Arena(plugin, name, displayName, spawnA, spawnB, spectatorSpawn);
    }

    public enum ArenaBuilderState {
        SET_SPEC, SET_A, SET_B
    }
}