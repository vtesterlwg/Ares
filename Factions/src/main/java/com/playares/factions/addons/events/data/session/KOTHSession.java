package com.playares.factions.addons.events.data.session;

import com.google.common.collect.Maps;
import com.playares.commons.base.util.Time;
import com.playares.factions.addons.events.data.timer.KOTHCountdownTimer;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public final class KOTHSession {
    @Getter public final KOTHEvent event;
    @Getter @Setter public boolean active;
    @Getter @Setter public PlayerFaction capturingFaction;
    @Getter @Setter public long captureChestUnlockTime;
    @Getter public final Map<PlayerFaction, Integer> leaderboard;
    @Getter @Setter public int ticketsNeededToWin;
    @Getter @Setter public int timerDuration;
    @Getter @Setter public KOTHCountdownTimer timer;

    public KOTHSession(KOTHEvent event, int ticketsNeededToWin, int timerDuration) {
        this.event = event;
        this.active = false;
        this.capturingFaction = null;
        this.captureChestUnlockTime = -1;
        this.leaderboard = Maps.newConcurrentMap();
        this.ticketsNeededToWin = ticketsNeededToWin;
        this.timerDuration = timerDuration;
        this.timer = new KOTHCountdownTimer(event, timerDuration);
        this.timer.freeze();
    }

    public boolean isCaptured() {
        return capturingFaction != null && !active;
    }

    public long getTimeUntilCaptureChestUnlock() {
        return captureChestUnlockTime - Time.now();
    }
}
