package com.riotmc.factions.addons.events.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.riotmc.commons.base.util.Time;
import com.riotmc.factions.addons.events.type.KOTHCounter;
import com.riotmc.factions.addons.events.type.KOTHTicket;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class KOTHTicketSession {
    @Getter @Setter public boolean active;
    @Getter @Setter public int winCondition;
    @Getter @Setter public int timerDuration;
    @Getter public KOTHCounter timer;
    @Getter @Setter public PlayerFaction capturingFaction;
    @Getter public Set<UUID> insidePlayers;
    @Getter public Map<PlayerFaction, Integer> tickets;

    public KOTHTicketSession(KOTHTicket event, int winCondition, int timerDuration) {
        this.event = event;
        this.active = false;
        this.winCondition = winCondition;
        this.timerDuration = timerDuration;
        this.timer = new KOTHCounter(event, timerDuration);
        this.capturingFaction = null;
        this.insidePlayers = Sets.newConcurrentHashSet();
        this.tickets = Maps.newConcurrentMap();
    }

    public boolean isInside(Player player) {
        return insidePlayers.contains(player.getUniqueId());
    }
}
