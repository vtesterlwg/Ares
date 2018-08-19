package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class FactionManageHandler {
    @Getter
    public FactionManager manager;

    public FactionManageHandler(FactionManager manager) {
        this.manager = manager;
    }

    public void rename(Player player, String name, SimplePromise promise) {
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean mod = player.hasPermission("factions.mod");

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !mod) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        if (name.equals(faction.getName())) {
            promise.failure("Faction is already named " + name);
            return;
        }

        if (!name.matches("^[A-Za-z0-9_.]+$")) {
            promise.failure("Faction names must only contain characters A-Z, 0-9");
            return;
        }

        if (name.length() < manager.getPlugin().getFactionConfig().getMinFactionNameLength()) {
            promise.failure("Name is too short (Min 3 characters)");
            return;
        }

        if (name.length() > manager.getPlugin().getFactionConfig().getMaxFactionNameLength()) {
            promise.failure("Name is too long (Max 16 characters)");
            return;
        }

        if (manager.getPlugin().getFactionConfig().getBannedFactionNames().contains(name)) {
            promise.failure("This faction name is not allowed");
            return;
        }

        if (manager.getFactionByName(name) != null) {
            promise.failure("Faction name is already in use");
            return;
        }

        Logger.print(player.getName() + " renamed " + faction.getName() + " to " + name);

        faction.setName(name);
        faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has updated the faction name to " + ChatColor.YELLOW + name);

        promise.success();
    }

    public void renameOther(Player player, String factionName, String name, SimplePromise promise) {
        final Faction faction = manager.getFactionByName(factionName);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        if (name.equals(faction.getName())) {
            promise.failure("Faction is already named " + name);
            return;
        }

        if (!name.matches("^[A-Za-z0-9_.]+$")) {
            promise.failure("Faction names must only contain characters A-Z, 0-9");
            return;
        }

        if (name.length() < manager.getPlugin().getFactionConfig().getMinFactionNameLength()) {
            promise.failure("Name is too short (Min 3 characters)");
            return;
        }

        if (name.length() > manager.getPlugin().getFactionConfig().getMaxFactionNameLength()) {
            promise.failure("Name is too long (Max 16 characters)");
            return;
        }

        if (manager.getFactionByName(name) != null) {
            promise.failure("Faction name is already in use");
            return;
        }

        Logger.print(player.getName() + " renamed " + faction.getName() + " to " + name);

        faction.setName(name);

        if (faction instanceof PlayerFaction) {
            final PlayerFaction pf = (PlayerFaction)faction;
            pf.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has updated the faction name to " + ChatColor.YELLOW + name);
        }

        promise.success();
    }
}