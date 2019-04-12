package com.playares.factions.addons.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class EventsManager {
    @Getter public final EventsAddon addon;
    @Getter public final EventsHandler handler;

    @Getter public final Set<AresEvent> eventRepository;

    public EventsManager(EventsAddon addon) {
        this.addon = addon;
        this.handler = new EventsHandler(this);
        this.eventRepository = Sets.newConcurrentHashSet();
    }

    public AresEvent getEventByName(String name) {
        return eventRepository.stream().filter(event -> event.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public AresEvent getEventByOwnerId(UUID ownerId) {
        return eventRepository.stream().filter(event -> event.getOwnerId().equals(ownerId)).findFirst().orElse(null);
    }

    public ImmutableList<AresEvent> getEventsThatShouldStart() {
        return ImmutableList.copyOf(eventRepository.stream().filter(AresEvent::shouldStart).collect(Collectors.toList()));
    }

    public ImmutableList<AresEvent> getEventsAlphabetical() {
        final List<AresEvent> events = Lists.newArrayList(eventRepository);
        events.sort(Comparator.comparing(AresEvent::getName));
        return ImmutableList.copyOf(events);
    }

    public ImmutableList<AresEvent> getActiveEvents() {
        final List<AresEvent> events = Lists.newArrayList();

        for (AresEvent event : eventRepository) {
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
