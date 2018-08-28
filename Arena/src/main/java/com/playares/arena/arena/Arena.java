package com.playares.arena.arena;

import com.playares.commons.bukkit.location.PLocatable;

import java.util.List;

public interface Arena {
    String getName();

    List<String> getAuthors();

    List<PLocatable> getSpawns();

    boolean isInUse();

    default boolean isConfigured() {
        return getName() != null && !getSpawns().isEmpty();
    }

    void setInUse(boolean b);

    default void setSpawnA(PLocatable spawn) {
        if (!getSpawns().isEmpty()) {
            getSpawns().set(0, spawn);
            return;
        }

        getSpawns().add(spawn);
    }

    default void setSpawnB(PLocatable spawn) {
        if (getSpawns().size() == 2) {
            getSpawns().set(1, spawn);
            return;
        }

        getSpawns().add(spawn);
    }
}