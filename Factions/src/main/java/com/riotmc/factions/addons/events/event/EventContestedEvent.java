package com.riotmc.factions.addons.events.event;

import com.riotmc.factions.addons.events.type.Contestable;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class EventContestedEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final Contestable event;

    private final boolean state;

    public EventContestedEvent(Contestable event, boolean state) {
        this.event = event;
        this.state = state;
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}