package com.playares.services.classes.event;

import com.google.common.collect.Lists;
import com.playares.services.classes.data.effects.ClassEffectable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.List;

public final class BardEffectEvent extends PlayerEvent implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final ClassEffectable consumable;

    @Getter
    public final List<Player> affectedEntities;

    @Getter @Setter
    public boolean cancelled;

    public BardEffectEvent(Player who, ClassEffectable consumable) {
        super(who);
        this.consumable = consumable;
        this.affectedEntities = Lists.newArrayList();

        for (LivingEntity nearby : getPlayer().getWorld().getNearbyLivingEntities(getPlayer().getLocation(), 16)) {
            if (!(nearby instanceof Player)) {
                continue;
            }

            final Player affected = (Player)nearby;
            affectedEntities.add(affected);
        }
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}