package com.riotmc.factions.addons.loggers.event;

import com.riotmc.factions.addons.loggers.CombatLogger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class LoggerDeathEvent extends Event implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final CombatLogger logger;

    @Getter
    public final Player killer;

    @Getter @Setter
    public boolean cancelled;

    public LoggerDeathEvent(CombatLogger logger, Player killer) {
        this.logger = logger;
        this.killer = killer;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}