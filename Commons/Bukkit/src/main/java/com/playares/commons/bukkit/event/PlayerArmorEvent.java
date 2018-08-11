package com.playares.commons.bukkit.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public final class PlayerArmorEvent extends PlayerEvent implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final ItemStack item;

    @Getter @Setter
    public boolean cancelled;

    public PlayerArmorEvent(Player who, ItemStack item) {
        super(who);
        this.item = item;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}