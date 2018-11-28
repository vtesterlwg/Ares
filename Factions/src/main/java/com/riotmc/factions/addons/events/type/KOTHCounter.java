package com.riotmc.factions.addons.events.type;

import com.riotmc.commons.bukkit.timer.Timer;
import com.riotmc.factions.addons.events.event.KOTHTickEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

public final class KOTHCounter extends Timer {
    @Getter public final KOTHEvent event;
    @Getter @Setter public PlayerFaction capper;

    public KOTHCounter(KOTHEvent event, int seconds) {
        super(seconds);
        this.event = event;
        this.capper = null;
    }

    @Override
    public void onFinish() {
        final KOTHTickEvent tickEvent = new KOTHTickEvent(event, capper);
        Bukkit.getPluginManager().callEvent(tickEvent);
    }
}
