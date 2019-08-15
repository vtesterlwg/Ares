package com.playares.factions.addons.events.builder.type;

import com.google.common.collect.Lists;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.addons.events.data.type.koth.PalaceEvent;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PalaceEventBuilder extends KOTHEventBuilder {
    public PalaceEventBuilder(EventsAddon addon, Player player) {
        super(addon, player);
    }

    @Override
    public void build(FailablePromise<KOTHEvent> promise) {
        final UUID owningFactionId = (owningFaction != null) ? owningFaction.getUniqueId() : null;

        if (name == null) {
            promise.failure("Name is not set");
            return;
        }

        if (displayName == null) {
            promise.failure("Display name is not set");
            return;
        }

        if (cornerA == null) {
            promise.failure("Corner A is not set");
            return;
        }

        if (cornerB == null) {
            promise.failure("Corner B is not set");
            return;
        }

        if (lootChest == null) {
            promise.failure("Loot chest is not set");
            return;
        }

        final PalaceEvent event = new PalaceEvent(addon, owningFactionId, name, displayName, Lists.newArrayList(), lootChest, cornerA, cornerB, 30, 60); // TODO: Make customizable
        promise.success(event);
    }
}