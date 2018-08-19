package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.timers.FactionTimer;
import com.playares.factions.timers.FactionTimerFactory;
import com.playares.factions.timers.cont.faction.DTRFreezeTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class FactionStaffHandler {
    @Getter
    public final FactionManager manager;

    public FactionStaffHandler(FactionManager manager) {
        this.manager = manager;
    }

    public void freeze(Player player, String name, String time, SimplePromise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(name);
        final long ms;

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        try {
            ms = Time.parseTime(time);
        } catch (NumberFormatException ex) {
            promise.failure("Invalid time");
            return;
        }

        faction.addTimer(FactionTimerFactory.createFreezeTimer(faction, ms));
        faction.sendMessage(ChatColor.RED + "Your power has been frozen for " + ChatColor.YELLOW + Time.convertToRemaining(ms));

        Logger.print(player.getName() + " froze " + faction.getName() + "'s power for " + Time.convertToRemaining(ms));

        promise.success();
    }

    public void unfreeze(Player player, String name, SimplePromise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(name);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        final DTRFreezeTimer timer = (DTRFreezeTimer)faction.getTimer(FactionTimer.FactionTimerType.FREEZE);

        if (timer == null) {
            promise.failure(faction.getName() + "'s DTR is not frozen");
            return;
        }

        timer.onFinish();
        faction.removeTimer(timer.getType());

        Logger.print(player.getName() + " thawed " + faction.getName() + "'s DTR");

        promise.success();
    }
}