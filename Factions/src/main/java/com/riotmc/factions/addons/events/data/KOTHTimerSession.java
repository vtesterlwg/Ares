package com.riotmc.factions.addons.events.data;

import com.riotmc.commons.bukkit.timer.Timer;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class KOTHTimerSession {
    @Getter @Setter public boolean active;
    @Getter @Setter public int winCondition;
    @Getter @Setter public boolean contested;
    @Getter public Timer timer;
    @Getter public Set<UUID> insidePlayers;
    @Getter @Setter public PlayerFaction capturingFaction;

    public boolean isInside(Player player) {
        return insidePlayers.contains(player.getUniqueId());
    }
}
