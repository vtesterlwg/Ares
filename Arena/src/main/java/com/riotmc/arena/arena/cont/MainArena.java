package com.riotmc.arena.arena.cont;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.location.PLocatable;
import com.riotmc.arena.arena.Arena;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.List;

public final class MainArena implements Arena {
    @Nonnull @Getter
    public final String name;

    @Nonnull @Getter @Setter
    public List<String> authors;

    @Nonnull @Getter @Setter
    public List<PLocatable> spawns;

    @Getter @Setter
    public boolean inUse;

    public MainArena(@Nonnull String name) {
        this.name = name;
        this.authors = Lists.newArrayList();
        this.spawns = Lists.newArrayListWithExpectedSize(2);
        this.inUse = false;
    }

    public MainArena(@Nonnull String name, @Nonnull List<String> authors, @Nonnull List<PLocatable> spawns) {
        this.name = name;
        this.authors = authors;
        this.spawns = spawns;
        this.inUse = false;
    }
}