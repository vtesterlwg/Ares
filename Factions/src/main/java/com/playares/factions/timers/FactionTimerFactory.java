package com.playares.factions.timers;

import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.timers.cont.faction.DTRFreezeTimer;

public final class FactionTimerFactory {
    public static DTRFreezeTimer createFreezeTimer(PlayerFaction faction, long milliseconds) {
        return new DTRFreezeTimer(faction, milliseconds);
    }
}