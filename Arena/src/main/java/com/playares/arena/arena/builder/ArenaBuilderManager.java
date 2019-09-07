package com.playares.arena.arena.builder;

import com.google.common.collect.Sets;
import com.playares.arena.arena.ArenaManager;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;

public final class ArenaBuilderManager {
    @Getter public final ArenaManager manager;
    @Getter public final ArenaBuilderHandler handler;
    @Getter public final Set<ArenaBuilder> builders;

    public ArenaBuilderManager(ArenaManager manager) {
        this.manager = manager;
        this.handler = new ArenaBuilderHandler(this);
        this.builders = Sets.newConcurrentHashSet();
    }

    ArenaBuilder getBuilder(Player player) {
        return builders.stream().filter(builder -> builder.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }
}
