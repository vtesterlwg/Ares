package com.playares.factions.event;

import com.playares.factions.claims.data.DefinedClaim;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerChangeClaimEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Location locationFrom;
    @Getter public final Location locationTo;
    @Getter public final DefinedClaim claimFrom;
    @Getter public final DefinedClaim claimTo;
    @Getter @Setter public boolean cancelled;

    public PlayerChangeClaimEvent(Player who, Location locationFrom, Location locationTo, DefinedClaim claimFrom, DefinedClaim claimTo) {
        super(who);
        this.locationFrom = locationFrom;
        this.locationTo = locationTo;
        this.claimFrom = claimFrom;
        this.claimTo = claimTo;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}