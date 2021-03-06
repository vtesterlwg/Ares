package com.playares.factions.addons.events.event;

import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class KOTHTickEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();

    @Getter public final KOTHEvent event;

    public KOTHTickEvent(KOTHEvent event) {
        this.event = event;
    }

    public PlayerFaction getCapturer() {
        return event.getSession().getCapturingFaction();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}