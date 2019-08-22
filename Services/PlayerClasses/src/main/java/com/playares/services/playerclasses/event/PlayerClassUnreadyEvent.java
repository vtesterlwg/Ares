package com.playares.services.playerclasses.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerClassUnreadyEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();

    public PlayerClassUnreadyEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
