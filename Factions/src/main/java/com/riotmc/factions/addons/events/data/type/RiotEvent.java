package com.riotmc.factions.addons.events.data.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.data.schedule.EventSchedule;
import com.riotmc.factions.factions.data.PlayerFaction;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public interface RiotEvent {
    UUID getOwnerId();

    String getName();

    String getDisplayName();

    List<EventSchedule> getSchedule();

    BLocatable getCaptureChestLocation();

    void setDisplayName(String name);

    void capture(PlayerFaction faction);

    @SuppressWarnings("MagicConstant")
    default boolean shouldStart() {
        Preconditions.checkArgument(getSchedule().isEmpty(), "Schedule is empty");

        final Calendar calendar = Calendar.getInstance();

        for (EventSchedule time : getSchedule()) {
            if (calendar.get(Calendar.DAY_OF_WEEK) == time.getDay() && calendar.get(Calendar.HOUR_OF_DAY) == time.getHour() && calendar.get(Calendar.MINUTE) == time.getMinute()) {
                return true;
            }
        }

        return false;
    }

    default long getTimeToNextSchedule() {
        Preconditions.checkArgument(getSchedule().isEmpty(), "Schedule is empty");

        final List<Long> times = Lists.newArrayList();

        getSchedule().forEach(time -> times.add(Time.getTimeUntil(time.getDay(), time.getHour(), time.getMinute())));

        if (times.isEmpty()) {
            return -1L;
        }

        times.sort(Comparator.naturalOrder());
        return times.get(0);
    }
}
