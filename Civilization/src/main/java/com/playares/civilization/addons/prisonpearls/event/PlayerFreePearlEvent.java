package com.playares.civilization.addons.prisonpearls.event;

import com.playares.civilization.addons.prisonpearls.data.PrisonPearl;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerFreePearlEvent extends Event implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();

    @Getter public final Player player;
    @Getter public final PrisonPearl pearl;
    @Getter @Setter public boolean cancelled;

    public PlayerFreePearlEvent(Player player, PrisonPearl pearl) {
        this.player = player;
        this.pearl = pearl;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
