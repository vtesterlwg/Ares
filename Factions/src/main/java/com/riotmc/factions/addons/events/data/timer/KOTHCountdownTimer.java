package com.riotmc.factions.addons.events.data.timer;

import com.riotmc.commons.bukkit.timer.Timer;
import com.riotmc.factions.addons.events.data.type.koth.KOTHEvent;
import com.riotmc.factions.addons.events.event.KOTHTickEvent;
import lombok.Getter;
import org.bukkit.Bukkit;

public final class KOTHCountdownTimer extends Timer {
    @Getter public final KOTHEvent event;

    public KOTHCountdownTimer(KOTHEvent event, int timerDuration) {
        super(timerDuration);
        this.event = event;
    }

    @Override
    public void onFinish() {
        final KOTHTickEvent tickEvent = new KOTHTickEvent(event);
        Bukkit.getPluginManager().callEvent(tickEvent);
    }
}
