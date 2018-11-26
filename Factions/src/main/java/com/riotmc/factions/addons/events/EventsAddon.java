package com.riotmc.factions.addons.events;

import com.riotmc.commons.bukkit.event.PlayerBigMoveEvent;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.Addon;
import com.riotmc.factions.addons.events.listener.CaptureRegionListener;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerQuitEvent;

public final class EventsAddon implements Addon {
    public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.YELLOW + "Events" + ChatColor.GOLD + "] " + ChatColor.RESET;

    @Getter public final Factions plugin;
    @Getter public boolean enabled;
    @Getter public EventManager manager;
    @Getter public CaptureRegionListener captureRegionListener;

    public EventsAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Events";
    }

    @Override
    public void prepare() {
        this.manager = new EventManager(this);
        this.captureRegionListener = new CaptureRegionListener(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        PlayerQuitEvent.getHandlerList().unregister(captureRegionListener);
        PlayerBigMoveEvent.getHandlerList().unregister(captureRegionListener);
    }
}