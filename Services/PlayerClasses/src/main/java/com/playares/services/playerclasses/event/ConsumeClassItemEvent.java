package com.playares.services.playerclasses.event;

import com.google.common.collect.Maps;
import com.playares.services.playerclasses.data.ClassConsumable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;
import java.util.UUID;

public final class ConsumeClassItemEvent extends Event implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Player player;
    @Getter public final Map<UUID, Boolean> affectedPlayers;
    @Getter public final ClassConsumable consumable;
    @Getter @Setter public boolean cancelled;

    public ConsumeClassItemEvent(Player player, ClassConsumable consumable) {
        this.player = player;
        this.affectedPlayers = Maps.newHashMap();
        this.consumable = consumable;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}