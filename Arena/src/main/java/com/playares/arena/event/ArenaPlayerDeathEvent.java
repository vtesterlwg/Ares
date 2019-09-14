package com.playares.arena.event;

import com.playares.arena.match.Match;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ArenaPlayerDeathEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Player killer;
    @Getter public final Match match;

    public ArenaPlayerDeathEvent(Player who, Player killer, Match match) {
        super(who);
        this.killer = killer;
        this.match = match;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
