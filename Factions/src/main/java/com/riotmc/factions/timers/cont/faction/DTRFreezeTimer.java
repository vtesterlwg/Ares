package com.riotmc.factions.timers.cont.faction;

import com.riotmc.factions.factions.data.PlayerFaction;
import com.riotmc.factions.timers.FactionTimer;
import org.bukkit.ChatColor;

/**
 * Represents a Faction's DTR freeze
 */
public final class DTRFreezeTimer extends FactionTimer {
    public DTRFreezeTimer(PlayerFaction owner, long milliseconds) {
        super(owner, FactionTimerType.FREEZE, milliseconds);
    }

    public DTRFreezeTimer(PlayerFaction owner, int seconds) {
        super(owner, FactionTimerType.FREEZE, seconds);
    }

    @Override
    public void onFinish() {
        owner.sendMessage(ChatColor.GREEN + "Your faction will now begin regenerating DTR");
    }
}