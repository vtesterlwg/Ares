package com.riotmc.factions.addons.events;

import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.Addon;
import com.riotmc.factions.addons.events.builder.EventBuilderManager;
import com.riotmc.factions.addons.events.builder.EventBuilderWand;
import com.riotmc.factions.addons.events.command.EventCommand;
import com.riotmc.services.customitems.CustomItemService;
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
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        this.manager = new EventsManager(this);
        this.builderManager = new EventBuilderManager(this);

        plugin.registerCommand(new EventCommand(this));

        if (customItemService != null) {
            customItemService.registerNewItem(new EventBuilderWand());
        } else {
            Logger.error("CustomItemService was not found while preparing " + getName() + " Addon. Will be unable to use the Event Builder Wand");
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}