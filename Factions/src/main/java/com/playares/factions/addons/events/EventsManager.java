package com.playares.factions.addons.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.addons.events.engine.EventTicker;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class EventsManager {
    @Getter public final EventsAddon addon;
    @Getter public final EventsHandler handler;
    @Getter public final Set<AresEvent> eventRepository;
    @Getter @Setter public EventTicker ticker;

    public EventsManager(EventsAddon addon) {
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
    public void load() {
        final YamlConfiguration config = getAddon().getPlugin().getConfig("events");

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
                    continue;
                }

                final PalaceEvent palace = new PalaceEvent(addon, ownerId, name, displayName, schedule, captureChest, cornerA, cornerB, ticketsNeeded, timerDuration);
                eventRepository.add(palace);
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

        if (event instanceof PalaceEvent) {
            config.set(path + ".type", EventType.KOTH_PALACE.name());
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
}
