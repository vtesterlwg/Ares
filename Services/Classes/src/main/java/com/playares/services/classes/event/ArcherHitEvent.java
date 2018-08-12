package com.playares.services.classes.event;

import com.playares.commons.bukkit.location.PLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ArcherHitEvent extends PlayerEvent implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final LivingEntity damaged;

    @Getter
    public final double initialDamage;

    @Getter @Setter
    public boolean cancelled;

    public ArcherHitEvent(Player who, LivingEntity damaged, double initialDamage) {
        super(who);
        this.damaged = damaged;
        this.initialDamage = initialDamage;
    }

    public double getDistance() {
        final PLocatable floorA = new PLocatable(getPlayer());
        final PLocatable floorB = new PLocatable(damaged);

        floorA.setY(0);
        floorB.setY(0);

        return floorA.distance(floorB);
    }

    public double getDamage() {
        return ((getDistance() / 5.0) * 0.2) + getInitialDamage();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
