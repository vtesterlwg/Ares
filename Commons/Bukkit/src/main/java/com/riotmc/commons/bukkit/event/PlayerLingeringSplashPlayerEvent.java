package com.riotmc.commons.bukkit.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when a lingering potion thrown by a player is applied to another player
 */
public final class PlayerLingeringSplashPlayerEvent extends Event implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final Player damager;

    @Getter
    public final Player damaged;

    @Getter
    public final AreaEffectCloud cloud;

    @Getter @Setter
    public boolean cancelled;

    public PlayerLingeringSplashPlayerEvent(Player damager, Player damaged, AreaEffectCloud cloud) {
        this.damager = damager;
        this.damaged = damaged;
        this.cloud = cloud;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}