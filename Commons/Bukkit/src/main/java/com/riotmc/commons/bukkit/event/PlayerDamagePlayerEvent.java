package com.riotmc.commons.bukkit.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Fires when a player is damaged by another player
 */
@ParametersAreNonnullByDefault
public final class PlayerDamagePlayerEvent extends Event implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final Player damager;

    @Getter
    public final Player damaged;

    @Getter
    public final DamageType type;

    @Getter
    public final double damage;

    @Getter @Setter
    public boolean cancelled;

    public PlayerDamagePlayerEvent(@Nonnull Player damager, @Nonnull Player damaged, @Nonnull DamageType type, double damage) {
        this.damager = damager;
        this.damaged = damaged;
        this.type = type;
        this.damage = damage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public enum DamageType {
        PHYSICAL,

        PROJECTILE
    }
}