package com.riotmc.factions.addons.events.type;

import com.riotmc.commons.bukkit.timer.Timer;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public final class KOTHTicketSession {
    @Getter @Setter public boolean active;
    @Getter @Setter public int winCondition;
    @Getter @Setter public boolean contested;
    @Getter public Timer timer;
    @Getter @Setter public PlayerFaction capturingFaction;
    @Getter public Map<PlayerFaction, Integer> tickets;
}
