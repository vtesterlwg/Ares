package com.playares.arena.kit;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import lombok.Getter;

import java.util.Set;

public final class KitManager {
    @Getter public final Arenas plugin;
    @Getter public final Set<Kit> kits;

    public KitManager(Arenas plugin) {
        this.plugin = plugin;
        this.kits = Sets.newConcurrentHashSet();
    }

    public Kit getKit(String name) {
        return kits.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
