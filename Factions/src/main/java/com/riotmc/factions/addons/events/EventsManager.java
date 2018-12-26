package com.riotmc.factions.addons.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.riotmc.factions.addons.events.data.type.RiotEvent;
import com.riotmc.factions.addons.events.data.type.koth.KOTHEvent;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class EventsManager {
    @Getter public final EventsAddon addon;
    @Getter public final EventsHandler handler;

    @Getter public final Set<RiotEvent> eventRepository;

    public EventsManager(EventsAddon addon) {
        this.addon = addon;
        this.handler = new EventsHandler(this);
        this.eventRepository = Sets.newConcurrentHashSet();
    }

    public RiotEvent getEventByName(String name) {
        return eventRepository.stream().filter(event -> event.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public RiotEvent getEventByOwnerId(UUID ownerId) {
        return eventRepository.stream().filter(event -> event.getOwnerId().equals(ownerId)).findFirst().orElse(null);
    }

    public ImmutableList<RiotEvent> getEventsThatShouldStart() {
        return ImmutableList.copyOf(eventRepository.stream().filter(RiotEvent::shouldStart).collect(Collectors.toList()));
    }

    public ImmutableList<RiotEvent> getEventsAlphabetical() {
        final List<RiotEvent> events = Lists.newArrayList(eventRepository);
        events.sort(Comparator.comparing(RiotEvent::getName));
        return ImmutableList.copyOf(events);
    }

    public ImmutableList<RiotEvent> getActiveEvents() {
        final List<RiotEvent> events = Lists.newArrayList();

        for (RiotEvent event : eventRepository) {
            if (event instanceof KOTHEvent) {
                final KOTHEvent koth = (KOTHEvent)event;

                if (koth.getSession() != null && koth.getSession().isActive()) {
                    events.add(koth);
                }
            }

            // Add more event types here
        }

        return ImmutableList.copyOf(events);
    }
}
