package com.playares.factions.addons.events.data.type.koth;

import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.schedule.EventSchedule;
import com.playares.factions.factions.data.PlayerFaction;

import java.util.Collection;
import java.util.UUID;

public final class PalaceEvent extends KOTHEvent {
    public PalaceEvent(EventsAddon addon,
                       UUID ownerId,
                       String name,
                       String displayName,
                       Collection<EventSchedule> schedule,
                       BLocatable captureChestLocation,
                       BLocatable captureCornerA,
                       BLocatable captureCornerB,
                       int defaultTicketsNeededToWin,
                       int defaultTimerDuration) {

        super(addon, ownerId, name, displayName, schedule, captureChestLocation, captureCornerA, captureCornerB, defaultTicketsNeededToWin, defaultTimerDuration);

    }

    @Override
    public void capture(PlayerFaction faction) {
        super.capture(faction);
    }
}
