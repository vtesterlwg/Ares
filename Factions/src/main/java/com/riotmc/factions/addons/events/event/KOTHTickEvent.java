package com.riotmc.factions.addons.events.event;

import com.riotmc.factions.addons.events.type.koth.KOTHEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class KOTHTickEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();

    @Getter public final KOTHEvent KOTH;
    @Getter public final PlayerFaction capper;

    public KOTHTickEvent(KOTHEvent KOTH, PlayerFaction capper) {
        this.KOTH = KOTH;
        this.capper = capper;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
