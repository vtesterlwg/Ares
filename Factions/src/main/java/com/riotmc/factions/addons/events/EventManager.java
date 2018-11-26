package com.riotmc.factions.addons.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.factions.addons.events.type.KOTHEvent;
import com.riotmc.factions.addons.events.type.KOTHTicket;
import com.riotmc.factions.addons.events.type.KOTHTimer;
import com.riotmc.factions.addons.events.type.RiotEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class EventManager {
    @Getter public final EventsAddon addon;
    @Getter public final Set<RiotEvent> eventRepository;

    public EventManager(EventsAddon addon) {
        this.addon = addon;
        this.eventRepository = Sets.newConcurrentHashSet();
    }

    public RiotEvent getEventByOwner(UUID uniqueId) {
        return eventRepository.stream().filter(e -> e.getOwnerId().equals(uniqueId)).findFirst().orElse(null);
    }

    public RiotEvent getEventByName(String name) {
        return eventRepository.stream().filter(e -> e.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public ImmutableList<RiotEvent> getActiveEvents() {
        final List<RiotEvent> events = Lists.newArrayList();

        eventRepository.stream().filter(e -> e instanceof KOTHTicket).forEach(e -> {
            final KOTHTicket kt = (KOTHTicket)e;

            if (kt.getSession().isActive()) {
                events.add(kt);
            }
        });

        eventRepository.stream().filter(e -> e instanceof KOTHTimer).forEach(e -> {
            final KOTHTimer kt = (KOTHTimer)e;

            if (kt.getSession().isActive()) {
                events.add(kt);
            }
        });

        return ImmutableList.copyOf(events);
    }

    public ImmutableList<RiotEvent> getEventsAlphabetical() {
        final List<RiotEvent> events = Lists.newArrayList(eventRepository);
        events.sort(Comparator.comparing(RiotEvent::getName));
        return ImmutableList.copyOf(events);
    }

    public ImmutableList<RiotEvent> getEventsInsideRegistered(Player player) {
        final List<RiotEvent> events = Lists.newArrayList();

        for (RiotEvent event : getActiveEvents()) {
            if (event instanceof KOTHTicket) {
                final KOTHTicket kt = (KOTHTicket)event;

                if (kt.getSession().isInside(player)) {
                    events.add(event);
                }
            }

            else if (event instanceof KOTHTimer) {
                final KOTHTimer kt = (KOTHTimer)event;

                if (kt.getSession().isInside(player)) {
                    events.add(event);
                }
            }
        }

        return ImmutableList.copyOf(events);
    }

    public ImmutableList<RiotEvent> getEventsInsideUnregistered(Player player) {
        final List<RiotEvent> events = Lists.newArrayList();
        final PLocatable location = new PLocatable(player);

        for (RiotEvent event : getActiveEvents()) {
            if (event instanceof KOTHTicket) {
                final KOTHTicket kt = (KOTHTicket)event;

                if (kt.getCaptureRegion().inside(location) && !kt.getSession().getInsidePlayers().contains(player.getUniqueId())) {
                    events.add(event);
                }
            }

            else if (event instanceof KOTHTimer) {
                final KOTHTimer kt = (KOTHTimer)event;

                if (kt.getCaptureRegion().inside(location) && !kt.getSession().getInsidePlayers().contains(player.getUniqueId())) {
                    events.add(event);
                }
            }
        }

        return ImmutableList.copyOf(events);
    }

    public boolean isContested(KOTHEvent koth) {
        final Set<PlayerFaction> factions = Sets.newHashSet();
        final Set<UUID> insidePlayers = Sets.newHashSet();

        if (koth instanceof KOTHTicket) {
            final KOTHTicket kt = (KOTHTicket)koth;
            insidePlayers.addAll(kt.getSession().getInsidePlayers());
        }

        else if (koth instanceof KOTHTimer) {
            final KOTHTimer kt = (KOTHTimer)koth;
            insidePlayers.addAll(kt.getSession().getInsidePlayers());
        }

        for (UUID id : insidePlayers) {
            final Player player = Bukkit.getPlayer(id);

            if (player == null) {
                continue;
            }

            final PlayerFaction faction = addon.getPlugin().getFactionManager().getFactionByPlayer(player.getUniqueId());

            if (faction == null) {
                continue;
            }

            if (factions.contains(faction)) {
                continue;
            }

            factions.add(faction);
        }

        return factions.size() > 1;
    }
}
