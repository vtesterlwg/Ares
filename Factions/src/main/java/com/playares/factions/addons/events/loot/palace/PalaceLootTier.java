package com.playares.factions.addons.events.loot.palace;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PalaceLootTier {
    TIER3("Tier 3 (Low)"), TIER2("Tier 2 (Medium)"), TIER1("Tier 1 (Best)");

    @Getter public final String displayName;
}
