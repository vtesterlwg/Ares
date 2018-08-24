package com.playares.factions.event;

import com.playares.factions.claims.DefinedClaim;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerChangeClaimEvent extends PlayerEvent implements Cancellable {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final DefinedClaim from;

    @Getter
    public final DefinedClaim to;

    @Getter @Setter
    public boolean cancelled;

    public PlayerChangeClaimEvent(Player who, DefinedClaim from, DefinedClaim to) {
        super(who);
        this.from = from;
        this.to = to;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
