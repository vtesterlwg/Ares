package com.riotmc.factions.addons.events.handler;

import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.timer.BossTimer;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.manager.EventManager;
import com.riotmc.factions.addons.events.menu.EventMenu;
import com.riotmc.factions.addons.events.type.RiotEvent;
import com.riotmc.factions.addons.events.type.koth.KOTHEvent;
import com.riotmc.factions.addons.events.type.koth.KOTHTicket;
import com.riotmc.factions.addons.events.type.koth.KOTHTimer;
import com.riotmc.factions.addons.events.type.koth.Palace;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

public final class EventHandler {
    @Getter public final EventManager manager;

    public EventHandler(EventManager manager) {
        this.manager = manager;
    }

    public void create(String name, String displayName, String eventType, BLocatable capCornerA, BLocatable capCornerB, SimplePromise promise) {
        if (manager.getEventByName(name) != null) {
            promise.failure("Event with the name '" + name + "' already exists");
            return;
        }

        if (eventType.equalsIgnoreCase("kothticket")) {
            final KOTHTicket event = new KOTHTicket(name, displayName, capCornerA, capCornerB);
            manager.getEventRepository().add(event);

            // TODO: Write to file
        } else if (eventType.equalsIgnoreCase("kothtimer")) {
            final KOTHTimer event = new KOTHTimer(name, displayName, capCornerA, capCornerB);
            manager.getEventRepository().add(event);
        } else if (eventType.equalsIgnoreCase("palace")) {
            final Palace event = new Palace(name, displayName, capCornerA, capCornerB);
            manager.getEventRepository().add(event);
        } else {
            promise.failure("Invalid event type");
            return;
        }

        Logger.print("Event '" + name + "' has been created");
        promise.success();
    }

    public void delete(RiotEvent event, SimplePromise promise) {
        if (!manager.getEventRepository().contains(event)) {
            promise.failure("Event not found");
            return;
        }

        if (manager.getActiveEvents().contains(event)) {
            event.cancel();
        }

        manager.getEventRepository().remove(event);

        // TODO: Wipe from file

        Logger.print(event.getName() + " has been deleted");

        promise.success();
    }

    public void rename(RiotEvent event, String name, SimplePromise promise) {
        Logger.print(event.getName() + " has been renamed to '" + name + "'");
        event.setName(name);

        // TODO: Save to file

        promise.success();
    }

    public void renameDisplay(RiotEvent event, String name, SimplePromise promise) {
        Logger.print(event.getDisplayName() + ChatColor.RESET + " has been renamed to '" + ChatColor.translateAlternateColorCodes('&', name) + ChatColor.RESET + "'");
        event.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        // TODO: Save to file

        promise.success();
    }

    public void start(RiotEvent event, SimplePromise promise) {
        if (manager.getActiveEvents().contains(event)) {
            promise.failure("Event is already active");
            return;
        }

        event.start();

        Bukkit.broadcastMessage(EventsAddon.PREFIX + event.getDisplayName() + ChatColor.YELLOW + " is now active");

        final BossTimer notification = new BossTimer(manager.getAddon().getPlugin(), event.getDisplayName() + ChatColor.YELLOW + " is now active", BarColor.YELLOW, BarStyle.SEGMENTED_10, BossTimer.BossTimerDuration.FIVE_SECONDS);
        Bukkit.getOnlinePlayers().forEach(notification::addPlayer);
        notification.start();

        Logger.print(event.getName() + " is now active");
        promise.success();
    }

    public void cancel(RiotEvent event, SimplePromise promise) {
        if (!manager.getActiveEvents().contains(event)) {
            promise.failure("Event is not active");
            return;
        }

        event.cancel();

        Bukkit.broadcastMessage(EventsAddon.PREFIX + event.getDisplayName() + ChatColor.YELLOW + " has been canceled");

        Logger.print(event.getName() + " has been canceled");
        promise.success();
    }

    public void capture(RiotEvent event, PlayerFaction winner, SimplePromise promise) {
        event.capture(winner);

        Bukkit.broadcastMessage(EventsAddon.PREFIX + event.getDisplayName() + ChatColor.YELLOW + " has been captured by " + ChatColor.GOLD + winner.getName());

        Logger.print(event.getName() + " has been captured by " + event.getName());
        promise.success();
    }

    public void reset(KOTHEvent koth) {
        String displayName = null;

        if (koth instanceof KOTHTicket) {
            final KOTHTicket ticket = (KOTHTicket)koth;

            ticket.setContested(false);
            ticket.getSession().getTimer().setCapper(null);
            ticket.getSession().getTimer().setExpire(Time.now() + (ticket.getSession().getTimerDuration() * 1000L));
            ticket.getSession().getTimer().freeze();

            displayName = ticket.getDisplayName();
        }

        else if (koth instanceof KOTHTimer) {
            final KOTHTimer timer = (KOTHTimer)koth;

            timer.setContested(false);
            timer.getSession().getTimer().setCapper(null);
            timer.getSession().getTimer().setExpire(Time.now() + (900 * 1000L)); // TODO: Make configurable
            timer.getSession().getTimer().freeze();

            displayName = timer.getDisplayName();
        }

        else if (koth instanceof Palace) {
            final Palace palace = (Palace)koth;

            palace.setContested(false);
            palace.getSession().getTimer().setCapper(null);
            palace.getSession().getTimer().setExpire(Time.now() + (1800 * 1000L)); // TODO: Make configurable
            palace.getSession().getTimer().freeze();

            displayName = palace.getDisplayName();
        }

        Bukkit.broadcastMessage(EventsAddon.PREFIX + displayName + ChatColor.YELLOW + " has been reset");
    }

    public void list(Player viewer) {
        final EventMenu menu = new EventMenu(manager.getAddon(), viewer);
        menu.open();
    }
}