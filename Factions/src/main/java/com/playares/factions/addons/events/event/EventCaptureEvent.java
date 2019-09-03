package com.playares.factions.addons.events.event;

import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class EventCaptureEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();

    @Getter public final AresEvent event;
    @Getter public final PlayerFaction faction;

    public EventCaptureEvent(AresEvent event, PlayerFaction faction) {
        this.event = event;
        this.faction = faction;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
