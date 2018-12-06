package com.riotmc.factions.addons.events.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public final class EventSchedule {
    @Getter @Setter public int day;
    @Getter @Setter public int hour;
    @Getter @Setter public int minute;
}