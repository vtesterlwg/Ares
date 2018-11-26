package com.riotmc.factions.addons.events.data;

import com.riotmc.commons.bukkit.timer.Timer;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class KOTHTicketSession {
    @Getter @Setter public boolean active;
    @Getter @Setter public int winCondition;
    @Getter public Timer timer;
    @Getter @Setter public PlayerFaction capturingFaction;
    @Getter public Set<UUID> insidePlayers;
    @Getter public Map<PlayerFaction, Integer> tickets;

    public boolean isInside(Player player) {
        return insidePlayers.contains(player.getUniqueId());
    }
}
