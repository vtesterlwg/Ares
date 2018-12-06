package com.riotmc.factions.addons.events.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.data.sessions.KOTHTicketSession;
import com.riotmc.factions.addons.events.data.sessions.KOTHTimerSession;
import com.riotmc.factions.addons.events.handler.EventHandler;
import com.riotmc.factions.addons.events.type.RiotEvent;
import com.riotmc.factions.addons.events.type.koth.KOTHEvent;
import com.riotmc.factions.addons.events.type.koth.KOTHTicket;
import com.riotmc.factions.addons.events.type.koth.KOTHTimer;
import com.riotmc.factions.addons.events.type.koth.Palace;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class EventManager {
    @Getter public final EventsAddon addon;
    @Getter public final EventHandler handler;
    @Getter public final Set<RiotEvent> eventRepository;
    @Getter public BukkitTask eventScheduler;

    public EventManager(EventsAddon addon) {
        this.addon = addon;
        this.handler = new EventHandler(this);
        this.eventRepository = Sets.newConcurrentHashSet();
    }

    public void start() {
        this.eventScheduler = new Scheduler(addon.getPlugin()).repeat(30 * 20L, 30 * 20L).async(() -> eventRepository.stream().filter(RiotEvent::shouldStart).forEach(event -> {
            if (event instanceof KOTHTicket) {
                final KOTHTicket koth = (KOTHTicket)event;

                if (koth.getSession() == null || !koth.getSession().isActive()) {
                    koth.setSession(new KOTHTicketSession(koth, 15, 60)); // TODO: Make configurable

                    handler.start(koth, new SimplePromise() {
                        @Override
                        public void success() {
                            Logger.print(koth.getName() + " has automatically started");
                        }

                        @Override
                        public void failure(@Nonnull String reason) {
                            Logger.error("Failed to start event '" + koth.getName() + "', Reason: " + reason);
                        }
                    });
                }
            }

            else if (event instanceof KOTHTimer) {
                final KOTHTimer koth = (KOTHTimer)event;

                if (koth.getSession() == null || !koth.getSession().isActive()) {
                    koth.setSession(new KOTHTimerSession(koth, 900)); // TODO: Make configurable

                    handler.start(koth, new SimplePromise() {
                        @Override
                        public void success() {
                            Logger.print(koth.getName() + " has automatically started");
                        }

                        @Override
                        public void failure(@Nonnull String reason) {
                            Logger.error("Failed to start event '" + koth.getName() + "', Reason: " + reason);
                        }
                    });
                }
            }

            else if (event instanceof Palace) {
                final Palace palace = (Palace)event;

                if (palace.getSession() == null || palace.getSession().isActive()) {
                    palace.setSession(new KOTHTicketSession(palace, 30, 60)); // TODO: Make configurable

                    handler.start(palace, new SimplePromise() {
                        @Override
                        public void success() {
                            Logger.print(palace.getName() + " has automatically started");
                        }

                        @Override
                        public void failure(@Nonnull String reason) {
                            Logger.error("Failed to start event '" + palace.getName() + "', Reason: " + reason);
                        }
                    });
                }
            }
        })).run();
    }

    public void stop() {
        if (eventScheduler != null && !eventScheduler.isCancelled()) {
            eventScheduler.cancel();
            eventScheduler = null;
        }
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

        else if (koth instanceof Palace) {
            final Palace palace = (Palace)koth;
            insidePlayers.addAll(palace.getSession().getInsidePlayers());
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
