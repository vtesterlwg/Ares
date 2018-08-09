package com.playares.arena.challenge;

import com.playares.arena.mode.Mode;

import java.util.UUID;

public interface Challenge {
    UUID getUniqueId();

    Mode getMode();
}
