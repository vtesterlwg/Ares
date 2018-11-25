package com.riotmc.factions.addons.events.type;

import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.data.CaptureRegion;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public final class Palace implements RiotEvent, KOTHEvent {
    @Getter @Setter public UUID ownerId;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter @Setter public CaptureRegion captureRegion;
    @Getter @Setter public KOTHTicketSession session;
    @Getter @Setter public PlayerFaction owner;
    @Getter @Setter public long nextLootRespawn;

    public Palace(String name, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = null;
        this.name = name;
        this.displayName = name;
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
        this.owner = null;
        this.nextLootRespawn = Time.now() + (3600 * 1000L);
    }

    public Palace(UUID ownerId, String name, String displayName, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
        this.owner = null;
        this.nextLootRespawn = Time.now() + (3600 * 1000L);
    }

    public Palace(UUID ownerId, String name, String displayName, BLocatable cornerA, BLocatable cornerB, PlayerFaction owner) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
        this.owner = owner;
        this.nextLootRespawn = Time.now() + (3600 * 1000L);
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

    }
}
