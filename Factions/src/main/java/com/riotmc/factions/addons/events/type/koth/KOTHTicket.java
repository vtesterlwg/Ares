package com.riotmc.factions.addons.events.type.koth;

import com.google.common.collect.Lists;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.data.CaptureRegion;
import com.riotmc.factions.addons.events.data.EventSchedule;
import com.riotmc.factions.addons.events.data.sessions.KOTHTicketSession;
import com.riotmc.factions.addons.events.type.Contestable;
import com.riotmc.factions.addons.events.type.RiotEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public final class KOTHTicket implements RiotEvent, KOTHEvent, Contestable {
    @Getter @Setter public UUID ownerId;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter @Setter public List<EventSchedule> schedule;
    @Getter @Setter public CaptureRegion captureRegion;
    @Getter @Setter public KOTHTicketSession session;
    @Getter @Setter public boolean contested;

    public KOTHTicket(String name, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = null;
        this.name = name;
        this.displayName = name;
        this.schedule = Lists.newArrayList();
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
    }

    public KOTHTicket(String name, String displayName, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = null;
        this.name = name;
        this.displayName = displayName;
        this.schedule = Lists.newArrayList();
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
    }

    public KOTHTicket(UUID ownerId, String name, String displayName, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.schedule = Lists.newArrayList();
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
    }

    public KOTHTicket(UUID ownerId, String name, String displayName, List<EventSchedule> schedule, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.schedule = schedule;
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
    }

    public void tick(PlayerFaction faction) {
        session.tick(faction);
    }

    @Override
    public void start() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void capture(PlayerFaction winner) {

    }
}