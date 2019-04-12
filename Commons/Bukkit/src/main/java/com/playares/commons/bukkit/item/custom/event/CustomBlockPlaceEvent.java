package com.playares.commons.bukkit.item.custom.event;

import com.playares.commons.bukkit.item.custom.CustomBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import javax.annotation.Nonnull;

/**
 * Fires when a custom block is placed
 */
public final class CustomBlockPlaceEvent extends PlayerEvent implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Nonnull @Getter
    public final CustomBlock block;

    @Getter @Setter
    public boolean cancelled;

    public CustomBlockPlaceEvent(Player who, CustomBlock block) {
        super(who);
        this.block = block;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
