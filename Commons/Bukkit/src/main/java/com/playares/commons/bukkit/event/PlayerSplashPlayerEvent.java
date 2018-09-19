package com.playares.commons.bukkit.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Fires when a splash potion thrown by a player is applied to another player
 */
@ParametersAreNonnullByDefault
public final class PlayerSplashPlayerEvent extends Event implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();
    
    @Getter
    public final Player damager;
    
    @Getter
    public final Player damaged;
    
    @Getter
    public final ThrownPotion potion;
    
    @Getter @Setter
    public boolean cancelled;
    
    public PlayerSplashPlayerEvent(Player damager, Player damaged, ThrownPotion potion) {
        this.damager = damager;
        this.damaged = damaged;
        this.potion = potion;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}