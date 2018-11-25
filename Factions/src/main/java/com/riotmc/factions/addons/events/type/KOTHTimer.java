package com.riotmc.factions.addons.events.type;

import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.data.CaptureRegion;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public final class KOTHTimer implements RiotEvent, KOTHEvent {
    @Getter @Setter public UUID ownerId;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter @Setter public CaptureRegion captureRegion;
    @Getter @Setter public KOTHTimerSession session;

    public KOTHTimer(String name, BLocatable cornerA, BLocatable cornerB) {
        this.name = name;
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
    }

    public KOTHTimer(UUID ownerId, String name, String displayName, BLocatable cornerA, BLocatable cornerB) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.captureRegion = new CaptureRegion(cornerA, cornerB);
        this.session = null;
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
