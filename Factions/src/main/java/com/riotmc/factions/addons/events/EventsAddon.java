package com.riotmc.factions.addons.events;

import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.Addon;
import com.riotmc.factions.addons.events.builder.EventBuilderManager;
import com.riotmc.factions.addons.events.command.EventCommand;
import lombok.Getter;
import org.bukkit.ChatColor;

public final class EventsAddon implements Addon {
    public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.YELLOW + "Events" + ChatColor.GOLD + "] " + ChatColor.RESET;

    @Getter public final Factions plugin;
    @Getter public boolean enabled;

    @Getter public EventsManager manager;
    @Getter public EventBuilderManager builderManager;

    public EventsAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Events";
    }

    @Override
    public void prepare() {
        this.manager = new EventsManager(this);
        this.builderManager = new EventBuilderManager(this);

        plugin.registerCommand(new EventCommand(this));
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}