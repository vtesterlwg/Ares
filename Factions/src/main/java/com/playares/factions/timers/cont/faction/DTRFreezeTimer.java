package com.playares.factions.timers.cont.faction;

import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.timers.FactionTimer;
import org.bukkit.ChatColor;

public final class DTRFreezeTimer extends FactionTimer {
    public DTRFreezeTimer(PlayerFaction owner, int seconds) {
        super(owner, FactionTimerType.FREEZE, seconds);
    }

    @Override
    public void onFinish() {
        owner.sendMessage(ChatColor.GREEN + "Your faction will now begin regenerating DTR");
    }
}