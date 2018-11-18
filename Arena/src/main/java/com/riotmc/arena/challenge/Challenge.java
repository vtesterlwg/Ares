package com.riotmc.arena.challenge;

import com.riotmc.arena.mode.Mode;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface Challenge {
    @Nonnull
    UUID getUniqueId();

    @Nonnull
    Mode getMode();
}
