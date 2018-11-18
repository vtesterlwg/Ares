package com.riotmc.factions.factions.handlers;

import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.factions.claims.DefinedClaim;
import com.riotmc.factions.factions.Faction;
import com.riotmc.factions.factions.FactionManager;
import com.riotmc.factions.factions.PlayerFaction;
import com.riotmc.factions.factions.ServerFaction;
import com.riotmc.services.profiles.ProfileService;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class FactionManageHandler {
    /** Owning Manager **/
    @Getter public FactionManager manager;

    public FactionManageHandler(FactionManager manager) {
        this.manager = manager;
    }

    /**
     * Renames a faction
     * @param player Player
     * @param name New Name
     * @param promise Promise
     */
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

    /**
     * Renames a faction in the third-person
     * @param player Player
     * @param factionName Renamed Faction Name
     * @param name New Name
     * @param promise Promise
     */
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

    /**
     * Promote a player to the next rank
     * @param player Promoting Player
     * @param name Promoted Username
     * @param promise Promise
     */
    public void promote(Player player, String name, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)manager.getPlugin().getService(ProfileService.class);
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean admin = player.hasPermission("factions.admin");

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        final PlayerFaction.FactionProfile profile = faction.getMember(player.getUniqueId());
        final boolean leader = profile.getRank().equals(PlayerFaction.FactionRank.LEADER);

        if (profile.getRank().equals(PlayerFaction.FactionRank.MEMBER) && !admin) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        profileService.getProfile(name, aresProfile -> {
            if (aresProfile == null) {
                promise.failure("Player not found");
                return;
            }

            final PlayerFaction.FactionProfile otherProfile = faction.getMember(aresProfile.getUniqueId());

            if (otherProfile == null) {
                promise.failure("This player is not in your faction");
                return;
            }

            if (otherProfile.getRank().isHigherOrEqual(profile.getRank()) && !admin && !leader) {
                promise.failure("This player has equal or higher ranking");
                return;
            }

            final PlayerFaction.FactionRank newRank = otherProfile.getRank().getNext();

            if (newRank == null) {
                promise.failure("Next rank level not found");
                return;
            }

            otherProfile.setRank(newRank);

            if (Bukkit.getPlayer(aresProfile.getUniqueId()) != null) {
                Bukkit.getPlayer(aresProfile.getUniqueId()).sendMessage(ChatColor.GREEN + "You have been promoted!");
                return;
            }

            faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " promoted " + ChatColor.DARK_GREEN + aresProfile.getUsername() + ChatColor.GOLD + " to " + ChatColor.BLUE + StringUtils.capitalise(newRank.name().toLowerCase()));
            Logger.print(player.getName() + " promoted " + aresProfile.getUsername() + " to " + newRank.name());
            promise.success();
        });
    }

    /**
     * Demotes a player to the next lowest rank
     * @param player Demoting Player
     * @param name Demoted Username
     * @param promise Promise
     */
    public void demote(Player player, String name, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)manager.getPlugin().getService(ProfileService.class);
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean admin = player.hasPermission("factions.admin");

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        final PlayerFaction.FactionProfile profile = faction.getMember(player.getUniqueId());
        final boolean leader = profile.getRank().equals(PlayerFaction.FactionRank.LEADER);

        if (profile.getRank().equals(PlayerFaction.FactionRank.MEMBER) && !admin) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        profileService.getProfile(name, aresProfile -> {
            if (aresProfile == null) {
                promise.failure("Player not found");
                return;
            }

            final PlayerFaction.FactionProfile otherProfile = faction.getMember(aresProfile.getUniqueId());

            if (otherProfile == null) {
                promise.failure("This player is not in your faction");
                return;
            }

            if (otherProfile.getRank().isHigherOrEqual(profile.getRank()) && !admin && !leader) {
                promise.failure("This player has equal or higher ranking");
                return;
            }

            final PlayerFaction.FactionRank newRank = otherProfile.getRank().getLower();

            if (newRank == null) {
                promise.failure("Lower rank level not found");
                return;
            }

            otherProfile.setRank(newRank);

            if (Bukkit.getPlayer(aresProfile.getUniqueId()) != null) {
                Bukkit.getPlayer(aresProfile.getUniqueId()).sendMessage(ChatColor.RED + "You have been demoted!");
                return;
            }

            faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " demoted " + ChatColor.DARK_GREEN + aresProfile.getUsername() + ChatColor.GOLD + " to " + ChatColor.BLUE + StringUtils.capitalise(newRank.name().toLowerCase()));
            Logger.print(player.getName() + " demoted " + aresProfile.getUsername() + " to " + newRank.name());
            promise.success();
        });
    }

    /**
     * Sets the home location for a faction
     * @param player Player
     * @param promise Promise
     */
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

    /**
     * Sets the home location for a faction in the third-person
     * @param player Player
     * @param factionName Faction Name
     * @param promise Promise
     */
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

    /**
     * Sets the flag for a Server Faction
     * @param player Player
     * @param name Faction Name
     * @param flagName Flag name
     * @param promise Promise
     */
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

    /**
     * Sets the display name for a Server Faction
     * @param player Player
     * @param factionName Faction Name
     * @param displayName New Display Name
     * @param promise Promise
     */
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

    /**
     * Sets the buffer radius for a Server Faction
     * @param player Player
     * @param factionName Faction Name
     * @param buffer Buffer Radius
     * @param promise Promise
     */
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