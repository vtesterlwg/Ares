package com.playares.arena.arena.cont;

import com.google.common.collect.Lists;
import com.playares.arena.arena.Arena;
import com.playares.commons.bukkit.location.PLocatable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public final class MainArena implements Arena {
    @Getter
    public final String name;

    @Getter @Setter
    public List<String> authors;

    @Getter @Setter
    public List<PLocatable> spawns;

    @Getter @Setter
    public boolean inUse;

    public MainArena(String name) {
        this.name = name;
        this.authors = Lists.newArrayList();
        this.spawns = Lists.newArrayListWithExpectedSize(2);
        this.inUse = false;
    }

    public MainArena(String name, List<String> authors, List<PLocatable> spawns) {
        this.name = name;
        this.authors = authors;
        this.spawns = spawns;
        this.inUse = false;
    }
}