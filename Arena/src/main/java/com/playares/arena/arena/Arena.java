package com.playares.arena.arena;

import com.playares.commons.bukkit.location.PLocatable;

import javax.annotation.Nonnull;
import java.util.List;

public interface Arena {
    @Nonnull
    String getName();

    @Nonnull
    List<String> getAuthors();

    @Nonnull
    List<PLocatable> getSpawns();

    boolean isInUse();

    default boolean isConfigured() {
        return !getSpawns().isEmpty();
    }

    void setInUse(boolean b);

    default void setSpawnA(@Nonnull PLocatable spawn) {
        if (!getSpawns().isEmpty()) {
            getSpawns().set(0, spawn);
            return;
        }

        getSpawns().add(spawn);
    }

    default void setSpawnB(@Nonnull PLocatable spawn) {
        if (getSpawns().size() == 2) {
            getSpawns().set(1, spawn);
            return;
        }

        getSpawns().add(spawn);
    }
}