package com.riotmc.factions.addons.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.riotmc.factions.addons.events.type.KOTHTicket;
import com.riotmc.factions.addons.events.type.KOTHTimer;
import com.riotmc.factions.addons.events.type.RiotEvent;
import lombok.Getter;

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
}
