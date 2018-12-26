package com.riotmc.factions.timers;

import com.riotmc.commons.bukkit.timer.Timer;
import com.riotmc.factions.factions.data.PlayerFaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a timer for Factions
 */
public abstract class FactionTimer extends Timer {
    /** The owner of this timer **/
    @Getter public PlayerFaction owner;
    /** The type of this timer **/
    @Getter public final FactionTimerType type;

    public FactionTimer(PlayerFaction owner, FactionTimerType type, long milliseconds) {
        super(milliseconds);
        this.owner = owner;
        this.type = type;
    }

    public FactionTimer(PlayerFaction owner, FactionTimerType type, int seconds) {
        super(seconds);
        this.owner = owner;
        this.type = type;
    }

    @AllArgsConstructor
    public enum FactionTimerType {
        FREEZE
    }
}