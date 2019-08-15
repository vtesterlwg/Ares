package com.playares.factions.addons.events;

import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.events.builder.type.KOTHEventBuilder;
import com.playares.factions.addons.events.builder.type.PalaceEventBuilder;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.addons.events.menu.EventsMenu;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class EventsHandler {
    @Getter public final EventsManager manager;

    public EventsHandler(EventsManager manager) {
        this.manager = manager;
    }

    /**
     * Opens the Events Menu
     * @param player Player
     * @param promise Promise
     */
    public void list(Player player, FailablePromise<EventsMenu> promise) {
        if (manager.getEventRepository().isEmpty()) {
            promise.failure("There are no events created");
            return;
        }

        final EventsMenu menu = new EventsMenu(manager.getAddon(), manager.getAddon().getPlugin(), player, "Events", 1);
        promise.success(menu);
    }

    /**
     * Starts the creation of a new event
     * @param player Player
     * @param type Event Type
     * @param promise Promise
     */
    public void create(Player player, String type, SimplePromise promise) {
        if (manager.getAddon().getBuilderManager().getBuilder(player) != null) {
            promise.failure("You are already building an event");
            return;
        }

        if (!type.equalsIgnoreCase("koth") && !type.equalsIgnoreCase("palace")) {
            promise.failure("Invaid event type - Valid types: 'koth', 'palace'");
            return;
        }

        if (type.equalsIgnoreCase("koth")) {
            final KOTHEventBuilder builder = new KOTHEventBuilder(manager.getAddon(), player);
            manager.getAddon().getBuilderManager().getBuilders().add(builder);
            promise.success();
            return;
        }

        if (type.equalsIgnoreCase("palace")) {
            final PalaceEventBuilder builder = new PalaceEventBuilder(manager.getAddon(), player);
            manager.getAddon().getBuilderManager().getBuilders().add(builder);
            promise.success();
            return;
        }

        // Add other event types here
    }

    public void delete(Player player, String name, SimplePromise promise) {

    }

    public void rename(Player player, String currentName, String newName, SimplePromise promise) {

    }

    /**
     * Starts an event
     * @param name Event Name
     * @param ticketsNeededToWin Tickets needed to win event
     * @param timerDuration Ticket timer duration
     * @param promise Promise
     */
    public void start(String name, int ticketsNeededToWin, int timerDuration, SimplePromise promise) {
        final AresEvent event = manager.getEventByName(name);

        if (event == null) {
            promise.failure("Event not found");
            return;
        }

        if (event instanceof KOTHEvent) {
            final KOTHEvent koth = (KOTHEvent)event;

            if (koth.getSession() != null && koth.getSession().isActive()) {
                promise.failure("Event is already activated");
                return;
            }

            if (!manager.getTicker().isStarted()) {
                manager.getTicker().start();
            }

            koth.start(ticketsNeededToWin, timerDuration);
            promise.success();
            return;
        }

        promise.failure("Event not found");
    }

    /**
     * Stops an event
     * @param name Event Name
     * @param promise Promise
     */
    public void stop(String name, SimplePromise promise) {
        final AresEvent event = manager.getEventByName(name);

        if (event == null) {
            promise.failure("Event not found");
            return;
        }

        if (event instanceof KOTHEvent) {
            final KOTHEvent koth = (KOTHEvent)event;

            if (koth.getSession() == null || !koth.getSession().isActive()) {
                promise.failure("This event is not active");
                return;
            }

            koth.stop();

            if (getManager().getActiveEvents().isEmpty() && getManager().getTicker().isStarted()) {
                getManager().getTicker().stop();
            }

            promise.success();
            return;
        }

        promise.failure("Event not found");
    }

    public void set(Player player, String name, String type, int value, SimplePromise promise) {

    }
}