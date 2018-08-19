package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class FactionDisbandHandler {
    @Getter
    public final FactionManager manager;

    public FactionDisbandHandler(FactionManager manager) {
        this.manager = manager;
    }

    public void leave(Player player, SimplePromise promise) {
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean mod = player.hasPermission("factions.mod");

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.isRaidable() && !mod) {
            promise.failure("You can not leave while your faction is raid-able");
            return;
        }

        if (faction.isFrozen() && !mod) {
            promise.failure("You can not leave while your DTR is frozen");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.LEADER)) {
            promise.failure("You must promote someone else to leader before leaving");
            return;
        }

        // TODO: Check if inside faction's claims, cancel if they are

        faction.getMembers().remove(faction.getMember(player.getUniqueId()));
        faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has " + ChatColor.RED + "left" + ChatColor.GOLD + " the faction");

        Logger.print(player.getName() + " has left " + faction.getName());

        promise.success();
    }
}