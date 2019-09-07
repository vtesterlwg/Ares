package com.playares.arena.arena;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.arena.builder.ArenaBuilderManager;
import com.playares.arena.arena.data.Arena;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

public final class ArenaManager {
    @Getter public Arenas plugin;
    @Getter public ArenaHandler handler;
    @Getter public final ArenaBuilderManager builderManager;
    @Getter public final Set<Arena> arenas;

    public ArenaManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new ArenaHandler(this);
        this.builderManager = new ArenaBuilderManager(this);
        this.arenas = Sets.newConcurrentHashSet();
    }

    public void load() {

    }

    public Arena obtainArena() {
        final Set<Arena> available = getAvailableArenas();

        if (available.isEmpty()) {
            return null;
        }

        final Arena arena = available.stream().findFirst().orElse(null);
        arena.setInUse(true);

        return arena;
    }

    public ImmutableSet<Arena> getAvailableArenas() {
        return ImmutableSet.copyOf(arenas.stream().filter(arena -> !arena.isInUse()).collect(Collectors.toSet()));
    }

    public Arena getArena(String name) {
        return arenas.stream().filter(arena -> arena.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}