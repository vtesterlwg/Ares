package com.riotmc.factions.addons.events.data.type.koth;

import com.riotmc.commons.bukkit.location.Locatable;
import com.riotmc.factions.addons.events.data.region.CaptureRegion;
import com.riotmc.factions.addons.events.data.session.KOTHSession;
import com.riotmc.factions.factions.PlayerFaction;

public interface KOTHEvent {
    KOTHSession getSession();

    CaptureRegion getCaptureRegion();

    void setSession(KOTHSession session);

    void setCaptureRegion(CaptureRegion region);

    void tick(PlayerFaction faction);

    default boolean insideCaptureRegion(Locatable location) {
        return getCaptureRegion().inside(location);
    }
}
