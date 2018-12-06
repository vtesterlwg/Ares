package com.riotmc.factions.addons.events.data.sessions;

import com.google.common.collect.Sets;
import com.riotmc.factions.addons.events.type.koth.KOTHCounter;
import com.riotmc.factions.addons.events.type.koth.KOTHTimer;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class KOTHTimerSession {
    @Getter public final KOTHTimer event;
    @Getter @Setter public boolean active;
    @Getter @Setter public boolean contested;
    @Getter public KOTHCounter timer;
    @Getter public Set<UUID> insidePlayers;
    @Getter @Setter public PlayerFaction capturingFaction;

    public KOTHTimerSession(KOTHTimer event, int timerDuration) {
        this.event = event;
        this.active = false;
        this.contested = false;
        this.timer = new KOTHCounter(event, timerDuration);
        this.insidePlayers = Sets.newConcurrentHashSet();
        this.capturingFaction = null;
    }

    public boolean isInside(Player player) {
        return insidePlayers.contains(player.getUniqueId());
    }
}
