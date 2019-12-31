package com.playares.civilization.addons.prisonpearls.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerPearlEvent extends Event implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();

    @Getter public final Player killer;
    @Getter public final Player killed;
    @Getter public final String reason;
    @Getter @Setter public int duration;
    @Getter @Setter public boolean cancelled;

    public PlayerPearlEvent(Player killer, Player killed, String reason, int duration) {
        this.killer = killer;
        this.killed = killed;
        this.reason = reason;
        this.duration = duration;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
