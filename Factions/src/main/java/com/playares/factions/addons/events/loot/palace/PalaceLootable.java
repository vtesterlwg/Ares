package com.playares.factions.addons.events.loot.palace;

import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.loot.Lootable;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public final class PalaceLootable extends Lootable {
    @Getter public final PalaceLootTier tier;

    public PalaceLootable(EventsAddon addon, PalaceLootTier tier, String materialName, String displayName, short data, int amount, Map<Enchantment, Integer> enchantments, int requiredPull, int totalPulls) {
        super(addon, materialName, displayName, data, amount, enchantments, requiredPull, totalPulls);
        this.tier = tier;
    }
}