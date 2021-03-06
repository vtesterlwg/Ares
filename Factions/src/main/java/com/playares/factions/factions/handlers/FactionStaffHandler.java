package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.factions.manager.FactionManager;
import com.playares.factions.timers.FactionTimer;
import com.playares.factions.timers.cont.faction.DTRFreezeTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class FactionStaffHandler {
    /** Owning Manager **/
    @Getter public final FactionManager manager;

    public FactionStaffHandler(FactionManager manager) {
        this.manager = manager;
    }

    /**
     * Freezes a faction's DTR
     * @param player Player
     * @param name Faction Name
     * @param time Time
     * @param promise Promise
     */
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

        faction.addTimer(new DTRFreezeTimer(faction, ms));
        faction.sendMessage(ChatColor.RED + "Your power has been frozen for " + ChatColor.YELLOW + Time.convertToRemaining(ms));

        Logger.print(player.getName() + " froze " + faction.getName() + "'s power for " + Time.convertToRemaining(ms));

        promise.success();
    }

    /**
     * Thaws a Faction's DTR
     * @param player Player
     * @param name Faction Name
     * @param promise Promise
     */
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

    /**
     * Updates the DTR for a Player Faction
     * @param player Player
     * @param name Faction Name
     * @param dtr DTR
     * @param promise Promise
     */
    public void updateDTR(Player player, String name, double dtr, SimplePromise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(name);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        faction.updateDTR(dtr);
        faction.sendMessage(ChatColor.GOLD + "Your DTR has been updated to " + ChatColor.YELLOW + String.format("%.2f", dtr));

        Logger.print(player.getName() + " updated " + faction.getName() + "'s DTR to " + dtr);

        promise.success();
    }

    public void updateReinvites(Player player, String name, int reinvites, SimplePromise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(name);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        faction.setReinvites(reinvites);
        faction.sendMessage(ChatColor.GOLD + "Your reinvites have been updated to " + ChatColor.YELLOW + reinvites);

        Logger.print(player.getName() + " updated " + faction.getName() + "'s Reinvites to " + reinvites);

        promise.success();
    }
}