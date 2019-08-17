package com.playares.factions.addons.events;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.addons.events.builder.type.KOTHEventBuilder;
import com.playares.factions.addons.events.builder.type.PalaceEventBuilder;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.addons.events.data.type.koth.PalaceEvent;
import com.playares.factions.addons.events.loot.Lootable;
import com.playares.factions.addons.events.loot.palace.PalaceLootChest;
import com.playares.factions.addons.events.loot.palace.PalaceLootTier;
import com.playares.factions.addons.events.loot.palace.PalaceLootable;
import com.playares.factions.addons.events.menu.EventsMenu;
import com.playares.factions.addons.events.menu.LootMenu;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public final class EventsHandler {
    @Getter public final EventsManager manager;

    public EventsHandler(EventsManager manager) {
        this.manager = manager;
    }

    public void restock(Player player, String eventName, SimplePromise promise) {
        final AresEvent event = getManager().getEventByName(eventName);

        if (!(event instanceof PalaceEvent)) {
            promise.failure("Event not found");
            return;
        }

        final PalaceEvent palace = (PalaceEvent)event;

        if (palace.getLootChests().isEmpty()) {
            promise.failure("This event does not have any loot chests loaded");
            return;
        }

        palace.stock();
        Logger.print(player.getName() + " restocked all chests for " + palace.getName());
        promise.success();
    }

    public void createChest(Player player, String eventName, String tierName, SimplePromise promise) {
        final AresEvent event = getManager().getEventByName(eventName);
        final Block target = player.getTargetBlock(null, 4);
        final PalaceLootTier tier;

        if (target == null || !target.getType().equals(Material.CHEST)) {
            promise.failure("Block is not a chest (if you believe this is an error stand within 4.0 blocks)");
            return;
        }

        if (!(event instanceof PalaceEvent)) {
            promise.failure("Event not found or is not a Palace event");
            return;
        }

        final PalaceEvent palace = (PalaceEvent)event;
        final PalaceLootChest existing = getManager().getAddon().getLootManager().getPalaceLootChestByBlock(target);

        if (existing != null) {
            promise.failure("This is already a Palace chest");
            return;
        }

        try {
            tier = PalaceLootTier.valueOf(tierName);
        } catch (IllegalArgumentException ex) {
            promise.failure("Tier not found");
            return;
        }

        final PalaceLootChest lootChest = new PalaceLootChest(manager.getAddon(), target.getWorld().getName(), target.getX(), target.getY(), target.getZ(), tier);
        palace.getLootChests().add(lootChest);

        // TODO: Save to file

        Logger.print(player.getName() + " created a Palace chest for " + event.getName() + " at " + lootChest.toString());
        promise.success();
    }

    public void showStandardLoot(Player player, FailablePromise<LootMenu> promise) {
        final List<Lootable> loot = Lists.newArrayList(getManager().getAddon().getLootManager().getStandardLootables());

        if (loot.isEmpty()) {
            promise.failure("No loot found");
            return;
        }

        final LootMenu menu = new LootMenu(getManager().getAddon(), getManager().getAddon().getPlugin(), player, "Standard Loot", 5);
        menu.populate(loot);

        promise.success(menu);
    }

    public void showPalaceLoot(Player player, String tierName, FailablePromise<LootMenu> promise) {
        final PalaceLootTier tier;

        try {
            tier = PalaceLootTier.valueOf(tierName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            final List<String> tierList = Lists.newArrayList();

            for (PalaceLootTier lootTier : PalaceLootTier.values()) {
                tierList.add(lootTier.name());
            }

            promise.failure("Invalid loot tier. Try: " + Joiner.on(", ").join(tierList));
            return;
        }

        final List<PalaceLootable> loot = Lists.newArrayList(getManager().getAddon().getLootManager().getPalaceLootByTier(tier));

        if (loot.isEmpty()) {
            promise.failure("No loot found");
            return;
        }

        final LootMenu menu = new LootMenu(getManager().getAddon(), getManager().getAddon().getPlugin(), player, "Palace Loot : " + tier.getDisplayName(), 5);
        menu.populatePalace(loot);

        promise.success(menu);
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