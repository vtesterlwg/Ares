package com.playares.factions.addons.events;

import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.events.builder.EventBuilderManager;
import com.playares.factions.addons.events.builder.EventBuilderWand;
import com.playares.factions.addons.events.command.EventCommand;
import com.playares.factions.addons.events.listener.EventListener;
import com.playares.factions.addons.events.loot.LootManager;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;

public final class EventsAddon implements Addon {
    public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.YELLOW + "Events" + ChatColor.GOLD + "] " + ChatColor.RESET;

    @Getter public final Factions plugin;
    @Getter public boolean enabled;

    @Getter public EventsManager manager;
    @Getter public EventBuilderManager builderManager;
    @Getter public LootManager lootManager;

    public EventsAddon(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Events";
    }

    @Override
    public void prepare() {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        this.manager = new EventsManager(this);
        this.builderManager = new EventBuilderManager(this);
        this.lootManager = new LootManager(this);

        plugin.registerCommand(new EventCommand(this));
        plugin.registerListener(new EventListener(this));

        if (customItemService != null) {
            customItemService.registerNewItem(new EventBuilderWand());
        } else {
            Logger.error("CustomItemService was not found while preparing " + getName() + " Addon. Will be unable to use the Event Builder Wand");
        }
    }

    @Override
    public void start() {
        lootManager.load();
    }

    @Override
    public void stop() {}
}