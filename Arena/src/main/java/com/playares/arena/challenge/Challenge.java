package com.playares.arena.challenge;

import com.playares.arena.mode.Mode;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface Challenge {
    @Nonnull
    UUID getUniqueId();

    @Nonnull
    Mode getMode();
}
