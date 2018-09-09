package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.factions.ServerFaction;
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

    public void rename(Player player, String factionName, String name, SimplePromise promise) {
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

    public void setHome(Player player, SimplePromise promise) {
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final DefinedClaim inside = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));
        final boolean mod = player.hasPermission("factions.mod");

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !mod) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        if (player.getLocation().getY() >= manager.getPlugin().getFactionConfig().getFactionHomeCap()) {
            promise.failure("Location is too high");
            return;
        }

        if (inside == null || !inside.getOwnerId().equals(faction.getUniqueId())) {
            promise.failure("You must be standing in your faction's claims to set the home location");
            return;
        }

        faction.updateHome(player);

        promise.success();
    }

    public void setHome(Player player, String factionName, SimplePromise promise) {
        final Faction faction = manager.getFactionByName(factionName);
        final DefinedClaim inside = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        if (inside == null || !inside.getOwnerId().equals(faction.getUniqueId())) {
            promise.failure("You are not inside the faction's claims");
            return;
        }

        if (faction instanceof ServerFaction) {
            final ServerFaction sf = (ServerFaction)faction;
            sf.setLocation(new PLocatable(player));
            Logger.print(player.getName() + " updated location for " + sf.getName());
            promise.success();
            return;
        }

        final PlayerFaction pf = (PlayerFaction)faction;
        pf.updateHome(player);
        promise.success();
    }

    public void setFlag(Player player, String name, String flagName, SimplePromise promise) {
        final ServerFaction faction = manager.getServerFactionByName(name);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        final ServerFaction.FactionFlag flag;

        try {
            flag = ServerFaction.FactionFlag.valueOf(flagName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            promise.failure("Invalid faction flag");
            return;
        }

        if (faction.getFlag().equals(flag)) {
            promise.failure("This faction is already using this flag");
            return;
        }

        faction.setFlag(flag);
        Logger.print(player.getName() + " updated flag for " + faction.getName() + " to " + flag.name());
        promise.success();
    }

    public void setDisplayName(Player player, String factionName, String displayName, SimplePromise promise) {
        final ServerFaction faction = manager.getServerFactionByName(factionName);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        final String formatted = ChatColor.translateAlternateColorCodes('&', displayName);

        faction.setDisplayName(formatted);
        Logger.print(player.getName() + " set display name for " + faction.getName() + " to " + formatted);
        promise.success();
    }

    public void setBuffer(Player player, String factionName, double buffer, SimplePromise promise) {
        final ServerFaction faction = manager.getServerFactionByName(factionName);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        faction.setBuffer(buffer);
        Logger.print(player.getName() + " updated buffer radius for " + faction.getName() + " to " + buffer);
        promise.success();
    }
}