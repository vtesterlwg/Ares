package com.riotmc.factions.addons.events.data.type.koth;

import com.google.common.collect.Lists;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.data.region.CaptureRegion;
import com.riotmc.factions.addons.events.data.schedule.EventSchedule;
import com.riotmc.factions.addons.events.data.session.KOTHSession;
import com.riotmc.factions.addons.events.data.type.RiotEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class KOTHTicketEvent implements RiotEvent, KOTHEvent {
    @Getter @Setter public UUID ownerId;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter public final List<EventSchedule> schedule;
    @Getter @Setter public KOTHSession session;
    @Getter @Setter public CaptureRegion captureRegion;

    public KOTHTicketEvent(UUID ownerId, String name, String displayName, Collection<EventSchedule> schedule, int timerDuration, int ticketsNeededToWin, BLocatable captureCornerA, BLocatable captureCornerB) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.schedule = Lists.newArrayList(schedule);
        this.session = new KOTHSession(ticketsNeededToWin, timerDuration);
        this.captureRegion = new CaptureRegion(captureCornerA, captureCornerB);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void capture(PlayerFaction faction) {

    }

    @Override
    public void tick(PlayerFaction faction) {

    }
}
