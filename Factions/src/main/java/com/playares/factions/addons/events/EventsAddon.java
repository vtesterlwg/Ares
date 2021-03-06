package com.playares.factions.addons.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.events.builder.EventBuilderManager;
import com.playares.factions.addons.events.builder.EventBuilderWand;
import com.playares.factions.addons.events.command.EventCommand;
import com.playares.factions.addons.events.command.PalaceCommand;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.listener.EventListener;
import com.playares.factions.addons.events.loot.LootManager;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.List;

public final class EventsAddon implements Addon {
    public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.YELLOW + "Events" + ChatColor.GOLD + "] " + ChatColor.RESET;

    @Getter public final Factions plugin;
    @Getter public boolean enabled;
    @Getter public EventsManager manager;
    @Getter public EventBuilderManager builderManager;
    @Getter public LootManager lootManager;

    public EventsAddon(Factions plugin) {
        this.plugin = plugin;
        this.manager = new EventsManager(this);
        this.builderManager = new EventBuilderManager(this);
        this.lootManager = new LootManager(this);
    }

    @Override
    public String getName() {
        return "Events";
    }

    @Override
    public void prepare() {
        manager.load();
        lootManager.load();
    }

    @Override
    public void start() {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        plugin.registerCommand(new EventCommand(this));
        plugin.registerCommand(new PalaceCommand(this));
        plugin.registerListener(new EventListener(this));

        if (customItemService != null) {
            customItemService.registerNewItem(new EventBuilderWand());
        } else {
            Logger.error("CustomItemService was not found while preparing " + getName() + " Addon. Will be unable to use the Event Builder Wand");
        }

        getPlugin().getCommandManager().getCommandCompletions().registerAsyncCompletion("events", c -> {
            final List<String> events = Lists.newArrayList();

            for (AresEvent event : manager.getEventRepository()) {
                events.add(event.getName());
            }

            return ImmutableList.copyOf(events);
        });
    }

    @Override
    public void stop() {
        if (manager.getTicker() != null && manager.getTicker().isStarted()) {
            manager.getTicker().stop();
        }

        if (lootManager.getPalaceLootTimer() != null && !lootManager.getPalaceLootTimer().isCancelled()) {
            lootManager.getPalaceLootTimer().cancel();
            lootManager.palaceLootTimer = null;
        }
    }
}