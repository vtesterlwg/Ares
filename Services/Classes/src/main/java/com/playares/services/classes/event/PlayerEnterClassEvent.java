package com.playares.services.classes.event;

import com.playares.services.classes.data.classes.AresClass;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerEnterClassEvent extends PlayerEvent implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final AresClass currentClass;

    @Getter @Setter
    public boolean cancelled;

    public PlayerEnterClassEvent(Player who, AresClass currentClass) {
        super(who);
        this.currentClass = currentClass;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}