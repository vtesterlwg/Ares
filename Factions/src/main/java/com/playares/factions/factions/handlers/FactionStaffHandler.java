package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.timers.FactionTimerFactory;
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

        Logger.print(player.getName() + " freezed " + faction.getName() + "'s power for " + Time.convertToRemaining(ms));

        promise.success();
    }
}