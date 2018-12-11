package com.riotmc.factions.addons.events.builder;

import com.google.common.collect.Sets;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.builder.type.EventBuilder;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;

public final class EventBuilderManager {
    @Getter public final EventsAddon addon;
    @Getter public final EventBuilderHandler handler;
    @Getter public final EventBuilderListener listener;
    @Getter public final Set<EventBuilder> builders;

    public EventBuilderManager(EventsAddon addon) {
        this.addon = addon;
        this.handler = new EventBuilderHandler(this);
        this.listener = new EventBuilderListener(addon);
        this.builders = Sets.newConcurrentHashSet();

        addon.getPlugin().registerListener(listener);
    }

    public EventBuilder getBuilder(Player player) {
        return builders.stream().filter(builder -> builder.getBuilder().equals(player.getUniqueId())).findFirst().orElse(null);
    }
}
