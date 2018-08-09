package com.playares.commons.bukkit.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Set;

public final class ProcessedChatEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();

    @Getter @Setter
    public String displayName;

    @Getter @Setter
    public String message;

    @Getter
    public final Set<Player> recipients;

    @Getter @Setter
    public boolean cancelled;

    public ProcessedChatEvent(Player who, String message, Set<Player> recipients) {
        super(who);
        this.displayName = who.getName();
        this.message = message;
        this.recipients = recipients;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
