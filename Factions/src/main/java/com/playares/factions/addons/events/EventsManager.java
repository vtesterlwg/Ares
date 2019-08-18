package com.playares.factions.addons.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.addons.events.data.schedule.EventSchedule;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.EventType;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.addons.events.data.type.koth.PalaceEvent;
import com.playares.factions.addons.events.engine.EventTicker;
import com.playares.factions.addons.events.loot.palace.PalaceLootTier;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public final class EventsManager {
    /** Events Addon **/
    @Getter public final EventsAddon addon;

    /** Handles all events functions **/
    @Getter public final EventsHandler handler;

    /** Event repository containing all of the loaded events **/
    @Getter public final Set<AresEvent> eventRepository;

    /** Event Ticker contains the timer ticking data and logic behind event ticking **/
    @Getter @Setter public EventTicker ticker;

    /** Event Scheduler which handles starting events automatically **/
    @Getter public EventScheduler scheduler;

    EventsManager(EventsAddon addon) {
        this.addon = addon;
        this.handler = new EventsHandler(this);
        this.eventRepository = Sets.newConcurrentHashSet();
        this.ticker = new EventTicker(addon);
        this.scheduler = new EventScheduler(this);

        getScheduler().start();
    }

    /**
     * Loads all events from events.yml in to the Event Repository
     */
    void load() {
        final YamlConfiguration config = getAddon().getPlugin().getConfig("events");

        if (config == null) {
            Logger.error("Failed to obtain config for events.yml");
            return;
        }

        for (String name : config.getConfigurationSection("events").getKeys(false)) {
            final String path = "events." + name + ".";
            final UUID ownerId = (config.get(path + "owner-id") != null) ? UUID.fromString(config.getString(path + "owner-id")) : null;
            final String displayName = ChatColor.translateAlternateColorCodes('&', config.getString(path + "display-name"));

            final String captureChestWorld = config.getString(path + "capture-chest-location.world");
            final double captureChestX = config.getDouble(path + "capture-chest-location.x");
            final double captureChestY = config.getDouble(path + "capture-chest-location.y");
            final double captureChestZ = config.getDouble(path + "capture-chest-location.z");

            final List<EventSchedule> schedule = Lists.newArrayList();
            final BLocatable captureChest = new BLocatable(captureChestWorld, captureChestX, captureChestY, captureChestZ);

            final EventType type;

            try {
                type = EventType.valueOf(config.getString(path + "type"));
            } catch (IllegalArgumentException ex) {
                Logger.error("Invalid event type");
                continue;
            }

            for (String dateValue : config.getStringList(path + "schedule")) {
                final String[] split = dateValue.split(":");

                if (split.length != 3) {
                    Logger.error("Invalid date schedule for " + name + ", '" + dateValue + "' is invalid");
                    continue;
                }

                int dayOfWeek;
                int hourOfDay;
                int minuteOfHour;

                try {
                    dayOfWeek = Integer.parseInt(split[0]);
                    hourOfDay = Integer.parseInt(split[1]);
                    minuteOfHour = Integer.parseInt(split[2]);
                } catch (NumberFormatException ex) {
                    Logger.error("Invalid date schedule for " + name + ", '" + dateValue + "' must only contain numbers");
                    continue;
                }

                schedule.add(new EventSchedule(dayOfWeek, hourOfDay, minuteOfHour));
            }

            if (type.equals(EventType.KOTH_STANDARD) || type.equals(EventType.KOTH_PALACE)) {
                final int timerDuration = config.getInt(path + "timer-duration");
                final int ticketsNeeded = config.getInt(path + "tickets-needed");

                double aX, aY, aZ;
                double bX, bY, bZ;
                String aWorld, bWorld;

                aX = config.getDouble(path + "capture-region.a.x");
                aY = config.getDouble(path + "capture-region.a.y");
                aZ = config.getDouble(path + "capture-region.a.z");
                aWorld = config.getString(path + "capture-region.a.world");

                bX = config.getDouble(path + "capture-region.b.x");
                bY = config.getDouble(path + "capture-region.b.y");
                bZ = config.getDouble(path + "capture-region.b.z");
                bWorld = config.getString(path + "capture-region.b.world");

                final BLocatable cornerA = new BLocatable(aWorld, aX, aY, aZ);
                final BLocatable cornerB = new BLocatable(bWorld, bX, bY, bZ);

                if (type.equals(EventType.KOTH_STANDARD)) {
                    final KOTHEvent koth = new KOTHEvent(addon, ownerId, name, displayName, schedule, captureChest, cornerA, cornerB, ticketsNeeded, timerDuration);
                    eventRepository.add(koth);
                } else {
                    final Map<PalaceLootTier, Long> unlockTimes = Maps.newHashMap();

                    for (PalaceLootTier tier : PalaceLootTier.values()) {
                        long unlockTime = config.getLong(path + "loot-unlock-times." + tier.name());
                        unlockTimes.put(tier, unlockTime);
                    }

                    final PalaceEvent palace = new PalaceEvent(addon, ownerId, name, displayName, schedule, captureChest, cornerA, cornerB, ticketsNeeded, timerDuration);
                    palace.getLootTierUnlockTimes().putAll(unlockTimes);

                    eventRepository.add(palace);
                }
            }
        }

        Logger.print("Loaded " + eventRepository.size() + " events");
    }

    /**
     * Saves the provided event to file
     * @param event AresEvent
     */
    public void save(AresEvent event) {
        final YamlConfiguration config = getAddon().getPlugin().getConfig("events");
        final String path = "events." + event.getName();
        final List<String> scheduleList = Lists.newArrayList();

        config.set(path + ".owner-id", (event.getOwnerId() != null) ? event.getOwnerId().toString() : null);
        config.set(path + ".display-name", event.getDisplayName());
        config.set(path + ".capture-chest-location.x", event.getCaptureChestLocation().getX());
        config.set(path + ".capture-chest-location.y", event.getCaptureChestLocation().getY());
        config.set(path + ".capture-chest-location.z", event.getCaptureChestLocation().getZ());
        config.set(path + ".capture-chest-location.world", event.getCaptureChestLocation().getWorldName());

        for (EventSchedule schedule : event.getSchedule()) {
            final String toString = schedule.getDay() + ":" + schedule.getHour() + ":" + schedule.getMinute();
            scheduleList.add(toString);
        }

        config.set(path + ".schedule", scheduleList);

        // Here we're setting event specific values
        if (event instanceof PalaceEvent) {
            final PalaceEvent palace = (PalaceEvent)event;

            config.set(path + ".type", EventType.KOTH_PALACE.name());

            // We save the loot-tier unlock times to 0L by default
            for (PalaceLootTier tier : palace.getLootTierUnlockTimes().keySet()) {
                config.set(path + ".loot-unlock-times." + tier.name(), palace.getLootTierUnlockTimes().getOrDefault(tier, 0L));
            }
        } else if (event instanceof KOTHEvent) {
            config.set(path + ".type", EventType.KOTH_STANDARD.name());
        }

        if (event instanceof KOTHEvent) {
            final KOTHEvent koth = (KOTHEvent)event;

            config.set(path + ".timer-duration", koth.getDefaultTimerDuration());
            config.set(path + ".tickets-needed", koth.getDefaultTicketsNeededToWin());

            config.set(path + ".capture-region.a.x", koth.getCaptureRegion().getCornerA().getX());
            config.set(path + ".capture-region.a.y", koth.getCaptureRegion().getCornerA().getY());
            config.set(path + ".capture-region.a.z", koth.getCaptureRegion().getCornerA().getZ());
            config.set(path + ".capture-region.a.world", koth.getCaptureRegion().getCornerA().getWorldName());

            config.set(path + ".capture-region.b.x", koth.getCaptureRegion().getCornerB().getX());
            config.set(path + ".capture-region.b.y", koth.getCaptureRegion().getCornerB().getY());
            config.set(path + ".capture-region.b.z", koth.getCaptureRegion().getCornerB().getZ());
            config.set(path + ".capture-region.b.world", koth.getCaptureRegion().getCornerB().getWorldName());
        }

        getAddon().getPlugin().saveConfig("events", config);
        Logger.print("Saved event '" + event.getName() + "' to file");
    }

    /**
     * Returns an AresEvent matching the provided name
     * @param name AresEvent name
     * @return AresEvent
     */
    public AresEvent getEventByName(String name) {
        return eventRepository.stream().filter(event -> event.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Returns an AresEvent matching the event ServerFaction UUID
     * @param ownerId ServerFaction UUID
     * @return AresEvent
     */
    public AresEvent getEventByOwnerId(UUID ownerId) {
        return eventRepository.stream().filter(event -> event.getOwnerId() != null && event.getOwnerId().equals(ownerId)).findFirst().orElse(null);
    }

    /**
     * Returns an Immutable List containing all events that should now start
     * @return ImmutableList containing AresEvents
     */
    public ImmutableList<AresEvent> getEventsThatShouldStart() {
        return ImmutableList.copyOf(eventRepository.stream().filter(AresEvent::shouldStart).collect(Collectors.toList()));
    }

    /**
     * Returns an Immutable List containing all Ares Events sorted by alphabetical order
     * @return ImmutableList containing AresEvents
     */
    public ImmutableList<AresEvent> getEventsAlphabetical() {
        final List<AresEvent> events = Lists.newArrayList(eventRepository);
        events.sort(Comparator.comparing(AresEvent::getName));
        return ImmutableList.copyOf(events);
    }

    /**
     * Returns an Immutable List containing all active AresEvents
     * @return ImmutableList containing AresEvents
     */
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

    /**
     * Returns an AresEvent with a loot chest location matching the provided block location
     * @param location Block Location
     * @return AresEvent
     */
    public AresEvent getEventByLootChest(BLocatable location) {
        for (AresEvent event : eventRepository) {
            if (
                    event.getCaptureChestLocation().getWorldName().equals(location.getWorldName())  &&
                    event.getCaptureChestLocation().getX() == location.getX() &&
                    event.getCaptureChestLocation().getY() == location.getY() &&
                    event.getCaptureChestLocation().getZ() == location.getZ()) {

                return event;

            }
        }

        return null;
    }

    /**
     * Returns an Immutable List containing all Palace Events
     * @return Immutable List containing Palace Events
     */
    public ImmutableList<PalaceEvent> getPalaceEvents() {
        final List<PalaceEvent> events = Lists.newArrayList();

        eventRepository.stream().filter(event -> event instanceof PalaceEvent).forEach(palaceEvent -> {
            events.add((PalaceEvent)palaceEvent);
        });

        return ImmutableList.copyOf(events);
    }
}
