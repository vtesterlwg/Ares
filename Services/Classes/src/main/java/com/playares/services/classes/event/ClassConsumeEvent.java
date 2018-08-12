package com.playares.services.classes.event;

import com.playares.services.classes.data.effects.ClassEffectable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ClassConsumeEvent extends PlayerEvent implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final ClassEffectable consumable;

    @Getter @Setter
    public boolean cancelled;

    public ClassConsumeEvent(Player who, ClassEffectable consumable) {
        super(who);
        this.consumable = consumable;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
