package com.riotmc.factions.event;

import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.factions.factions.data.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public final class MemberDeathEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final UUID uniqueId;
    @Getter public final String username;
    @Getter public final PlayerFaction faction;
    @Getter public final PLocatable locatable;
    @Getter @Setter public double subtractedDTR;
    @Getter @Setter public int freezeDuration;

    public MemberDeathEvent(UUID uniqueId, String username, PlayerFaction faction, PLocatable locatable, double subtractedDTR, int freezeDuration) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.faction = faction;
        this.locatable = locatable;
        this.subtractedDTR = subtractedDTR;
        this.freezeDuration = freezeDuration;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
