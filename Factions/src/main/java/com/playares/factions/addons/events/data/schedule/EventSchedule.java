package com.playares.factions.addons.events.data.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class EventSchedule {
    @Getter public final int day;
    @Getter public final int hour;
    @Getter public final int minute;
}
