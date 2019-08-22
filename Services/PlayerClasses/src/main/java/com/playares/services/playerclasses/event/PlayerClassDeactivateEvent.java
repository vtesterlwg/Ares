package com.playares.services.playerclasses.event;

import com.playares.services.playerclasses.data.Class;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerClassDeactivateEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Class playerClass;

    public PlayerClassDeactivateEvent(Player who, Class playerClass) {
        super(who);
        this.playerClass = playerClass;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}