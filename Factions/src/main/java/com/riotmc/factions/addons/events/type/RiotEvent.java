package com.riotmc.factions.addons.events.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.riotmc.commons.base.util.Time;
import com.riotmc.factions.addons.events.data.EventSchedule;
import com.riotmc.factions.factions.PlayerFaction;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public interface RiotEvent {
    UUID getOwnerId();

    String getName();

    String getDisplayName();

    List<EventSchedule> getSchedule();

    void setName(String name);

    void setDisplayName(String displayName);

    void start();

    void cancel();

    void capture(PlayerFaction winner);

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

    default long getTimeUntilNextSchedule() {
        Preconditions.checkArgument(getSchedule().isEmpty(), "Schedule is empty");

        final List<Long> times = Lists.newArrayList();
        final Calendar calendar = Calendar.getInstance();

        for (EventSchedule time : getSchedule()) {
            calendar.set(Calendar.DAY_OF_WEEK, time.getDay());
            calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
            calendar.set(Calendar.MINUTE, time.getMinute());

            final long ms = Time.getTimeUntil(time.getDay(), time.getHour(), time.getMinute());

            times.add(ms);
        }

        if (times.isEmpty()) {
            return 0L;
        }

        times.sort(Comparator.naturalOrder());
        return times.get(0);
    }
}
