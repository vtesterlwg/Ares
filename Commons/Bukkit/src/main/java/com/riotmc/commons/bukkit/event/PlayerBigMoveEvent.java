package com.riotmc.commons.bukkit.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Fires when a player moves a full block
 */
@ParametersAreNonnullByDefault
public final class PlayerBigMoveEvent extends PlayerEvent implements Cancellable {
    @Getter
    public static HandlerList handlerList = new HandlerList();

    @Getter
    public final Location from;

    @Getter
    public final Location to;

    @Getter @Setter
    public boolean cancelled;

    public PlayerBigMoveEvent(@Nonnull Player who, @Nonnull Location from, @Nonnull Location to) {
        super(who);
        this.from = from;
        this.to = to;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}