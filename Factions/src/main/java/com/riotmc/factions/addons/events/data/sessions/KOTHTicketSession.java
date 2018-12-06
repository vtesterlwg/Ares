package com.riotmc.factions.addons.events.data.sessions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.riotmc.commons.base.util.Time;
import com.riotmc.factions.addons.events.type.koth.KOTHCounter;
import com.riotmc.factions.addons.events.type.koth.KOTHEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;

public final class KOTHTicketSession {
    @Getter public final KOTHEvent event;
    @Getter @Setter public boolean active;
    @Getter @Setter public int winCondition;
    @Getter @Setter public int timerDuration;
    @Getter public KOTHCounter timer;
    @Getter @Setter public PlayerFaction capturingFaction;
    @Getter public Set<UUID> insidePlayers;
    @Getter public Map<PlayerFaction, Integer> tickets;

    public KOTHTicketSession(KOTHEvent event, int winCondition, int timerDuration) {
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

    public int getTickets(PlayerFaction faction) {
        return tickets.getOrDefault(faction, 0);
    }

    public ImmutableList<PlayerFaction> getTicketLeaderboard() {
        final List<PlayerFaction> factions = Lists.newArrayList(tickets.keySet());
        factions.sort(Comparator.comparingInt(this::getTickets));
        Collections.reverse(factions);
        return ImmutableList.copyOf(factions);
    }

    public void tick(PlayerFaction faction) {
        final int currentTickets = getTickets(faction);
        tickets.put(faction, currentTickets);

        tickets.keySet().stream().filter(f -> !f.getUniqueId().equals(faction.getUniqueId())).forEach(f -> {
            final int enemyTicketCurrent = getTickets(f);
            final int enemyTicketSubtracted = (enemyTicketCurrent - 2);

            if (enemyTicketSubtracted <= 0) {
                tickets.remove(f);
            } else {
                tickets.put(f, enemyTicketSubtracted);
            }
        });

        getTimer().setExpire(Time.now() + (timerDuration * 1000L));
    }
}
