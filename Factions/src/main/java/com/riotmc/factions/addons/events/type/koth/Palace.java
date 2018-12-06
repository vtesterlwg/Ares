package com.riotmc.factions.addons.events.type.koth;

import com.google.common.collect.Lists;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.data.CaptureRegion;
import com.riotmc.factions.addons.events.data.EventSchedule;
import com.riotmc.factions.addons.events.data.sessions.KOTHTicketSession;
import com.riotmc.factions.addons.events.type.RiotEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.UUID;

public final class Palace implements RiotEvent, KOTHEvent {
    @Getter @Setter public UUID ownerId;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter @Setter public List<EventSchedule> schedule;
    @Getter @Setter public CaptureRegion captureRegion;
    @Getter @Setter public KOTHTicketSession session;
    @Getter @Setter public PlayerFaction owningFaction;
    @Getter @Setter public long nextLootRespawn;
    @Getter @Setter public boolean contested;

    public Palace(String name, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = null;
        this.name = name;
        this.displayName = name;
        this.schedule = Lists.newArrayList();
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
        this.owningFaction = null;
        this.nextLootRespawn = Time.now() + (3600 * 1000L);
    }

    public Palace(String name, String displayName, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = null;
        this.name = name;
        this.displayName = displayName;
        this.schedule = Lists.newArrayList();
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
        this.owningFaction = null;
        this.nextLootRespawn = Time.now() + (3600 * 1000L);
    }

    public Palace(UUID ownerId, String name, String displayName, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.schedule = Lists.newArrayList();
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
        this.owningFaction = null;
        this.nextLootRespawn = Time.now() + (3600 * 1000L);
    }

    public Palace(UUID ownerId, String name, String displayName, BLocatable cornerA, BLocatable cornerB, PlayerFaction owner) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.schedule = Lists.newArrayList();
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
        this.owningFaction = owner;
        this.nextLootRespawn = Time.now() + (3600 * 1000L);
    }

    public Palace(UUID ownerId, String name, String displayName, List<EventSchedule> schedule, BLocatable cornerA, BLocatable cornerB, PlayerFaction owner) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.schedule = schedule;
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.owningFaction = owner;
    }

    public boolean shouldLootRespawn() {
        return nextLootRespawn <= Time.now();
    }

    @Override
    public void start() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void capture(PlayerFaction winner) {
        this.owningFaction = winner;

        winner.sendMessage(EventsAddon.PREFIX + ChatColor.YELLOW + "Your faction now controls " + displayName);
    }
}
