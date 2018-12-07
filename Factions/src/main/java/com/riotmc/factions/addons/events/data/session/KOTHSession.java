package com.riotmc.factions.addons.events.data.session;

import com.google.common.collect.Maps;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public final class KOTHSession {
    @Getter @Setter public boolean active;
    @Getter @Setter public PlayerFaction capturingFaction;
    @Getter @Setter public long captureChestUnlockTime;
    @Getter @Setter public BLocatable captureChestLocation;
    @Getter public final Map<PlayerFaction, Integer> leaderboard;
    @Getter @Setter public int ticketsNeededToWin;
    @Getter @Setter public int timerDuration;

    public KOTHSession(int ticketsNeededToWin, int timerDuration) {
        this.active = false;
        this.capturingFaction = null;
        this.captureChestUnlockTime = -1;
        this.leaderboard = Maps.newConcurrentMap();
        this.ticketsNeededToWin = ticketsNeededToWin;
        this.timerDuration = timerDuration;
    }

    public boolean isCaptured() {
        return capturingFaction != null && !active;
    }

    public long getTimeUntilCaptureChestUnlock() {
        return captureChestUnlockTime - Time.now();
    }
}
