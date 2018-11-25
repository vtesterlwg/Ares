package com.riotmc.factions.addons.events;

import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.timer.BossTimer;
import com.riotmc.factions.addons.events.type.RiotEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public final class EventHandler {
    @Getter public final EventManager manager;

    public EventHandler(EventManager manager) {
        this.manager = manager;
    }

    public void create() {

    }

    public void delete() {

    }

    public void rename() {

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
}
