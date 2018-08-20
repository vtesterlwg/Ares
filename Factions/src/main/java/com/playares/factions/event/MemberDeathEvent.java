package com.playares.factions.event;

import com.playares.commons.bukkit.location.PLocatable;
import com.playares.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class MemberDeathEvent extends PlayerEvent {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final PlayerFaction faction;

    @Getter
    public final PLocatable locatable;

    @Getter @Setter
    public double subtractedDTR;

    public MemberDeathEvent(Player who, PlayerFaction faction, PLocatable locatable, double subtractedDTR) {
        super(who);
        this.faction = faction;
        this.locatable = locatable;
        this.subtractedDTR = subtractedDTR;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
