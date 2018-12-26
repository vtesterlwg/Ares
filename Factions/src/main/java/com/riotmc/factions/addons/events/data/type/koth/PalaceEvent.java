package com.riotmc.factions.addons.events.data.type.koth;

import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.data.schedule.EventSchedule;
import com.riotmc.factions.factions.data.PlayerFaction;

import java.util.Collection;
import java.util.UUID;

public final class PalaceEvent extends KOTHEvent {
    public PalaceEvent(UUID ownerId, String name, String displayName, Collection<EventSchedule> schedule, BLocatable captureChestLocation, BLocatable captureCornerA, BLocatable captureCornerB) {
        super(ownerId, name, displayName, schedule, captureChestLocation, captureCornerA, captureCornerB);
    }

    @Override
    public void capture(PlayerFaction faction) {
        super.capture(faction);
    }
}
