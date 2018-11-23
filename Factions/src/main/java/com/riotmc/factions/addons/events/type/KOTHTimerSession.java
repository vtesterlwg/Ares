package com.riotmc.factions.addons.events.type;

import com.riotmc.commons.bukkit.timer.Timer;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

public final class KOTHTimerSession {
    @Getter @Setter public boolean active;
    @Getter @Setter public int winCondition;
    @Getter @Setter public boolean contested;
    @Getter public Timer timer;
    @Getter @Setter public PlayerFaction capturingFaction;
}
