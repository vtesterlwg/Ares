package com.playares.factions.addons.events.loot.palace;

import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.addons.events.EventsAddon;
import lombok.Getter;

public final class PalaceLootChest extends BLocatable {
    @Getter public final EventsAddon addon;
    @Getter public final PalaceLootTier tier;

    public PalaceLootChest(EventsAddon addon, String worldName, double x, double y, double z, PalaceLootTier tier) {
        super(worldName, x, y, z);
        this.addon = addon;
        this.tier = tier;
    }

    public void stock() {
        addon.getLootManager().fillPalaceChest(this);
    }
}