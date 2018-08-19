package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import com.playares.services.profiles.ProfileService;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class FactionCreationHandler {
    @Getter
    public final FactionManager manager;

    public FactionCreationHandler(FactionManager manager) {
        this.manager = manager;
    }

    public void createFaction(Player player, String name, SimplePromise promise) {
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

        if (manager.getFactionByPlayer(player.getUniqueId()) != null) {
            promise.failure("You are already in a faction");
            return;
        }

        if (manager.getFactionByName(name) != null) {
            promise.failure("Faction name is already in use");
            return;
        }

        final PlayerFaction faction = new PlayerFaction(manager.getPlugin(), name);

        faction.addMember(player.getUniqueId(), PlayerFaction.FactionRank.LEADER);
        faction.getMemberHistory().add(player.getUniqueId());
        faction.registerFriendly(player);

        player.setScoreboard(faction.getScoreboard());

        manager.getFactionRepository().add(faction);

        Logger.print(player.getName() + " created faction " + name);

        promise.success();
    }

    public void createServerFaction(Player player, String name, SimplePromise promise) {

    }

    public void sendInvite(Player player, String username, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)manager.getPlugin().getService(ProfileService.class);
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean admin = player.hasPermission("factions.admin");

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !admin) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        if (profileService == null) {
            promise.failure("Failed to obtain the Profile Service");
            return;
        }

        profileService.getProfile(username, profile -> {
            if (profile == null) {
                promise.failure("Player not found");
                return;
            }

            if (faction.getPendingInvites().contains(profile.getUniqueId())) {
                promise.failure("This player already has a pending invitation");
                return;
            }

            faction.getPendingInvites().add(profile.getUniqueId());
            faction.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.GOLD + " has invited " + ChatColor.YELLOW + profile.getUsername() + ChatColor.GOLD + " to the faction");

            if (faction.isRaidable()) {
                player.sendMessage(ChatColor.RED + profile.getUsername() + " will not be able to join until your faction is no longer raidable");
            }

            if (faction.isFrozen()) {
                player.sendMessage(ChatColor.RED + profile.getUsername() + " will not be able to join until your faction DTR is no longer frozen");
            }

            if (faction.getMembers().size() >= manager.getPlugin().getFactionConfig().getFactionMemberCap()) {
                player.sendMessage(ChatColor.RED + profile.getUsername() + " will not be able to join until your faction has less than " + manager.getPlugin().getFactionConfig().getFactionMemberCap() + " members");
            }

            if (faction.isReinvited(profile.getUniqueId())) {
                if (faction.getReinvites() > 0) {
                    player.sendMessage(ChatColor.RED + profile.getUsername() + " has left this faction recently and will consume a re-invite upon joining");
                } else {
                    player.sendMessage(ChatColor.RED + profile.getUsername() + " will not be able to join until your faction obtains more re-invites");
                }
            }

            if (Bukkit.getPlayer(profile.getUniqueId()) != null) {
                Bukkit.getPlayer(profile.getUniqueId()).sendMessage(
                        new ComponentBuilder
                                (player.getName())
                                .color(net.md_5.bungee.api.ChatColor.GOLD)
                                .append(" has invited you to join ")
                                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                .append(faction.getName())
                                .color(net.md_5.bungee.api.ChatColor.AQUA)
                                .append(".")
                                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                .append(" Type ")
                                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                .append("/f accept " + faction.getName())
                                .color(net.md_5.bungee.api.ChatColor.GOLD)
                                .append(" or ")
                                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                .append("click here")
                                .underlined(true)
                                .color(net.md_5.bungee.api.ChatColor.GOLD)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f accept " + faction.getName()))
                                .append(" to join")
                                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                .underlined(false)
                                .create());
            }

            promise.success();
        });
    }

    public void revokeInvite(Player player, String username, SimplePromise promise) {
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

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !admin) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        profileService.getProfile(username, profile -> {
            if (profile == null) {
                promise.failure("Player not found");
                return;
            }

            if (!faction.getPendingInvites().contains(profile.getUniqueId())) {
                promise.failure(profile.getUsername() + " does not have a pending invitation");
                return;
            }

            faction.getPendingInvites().remove(profile.getUniqueId());
            faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " revoked " + ChatColor.YELLOW + profile.getUsername() + ChatColor.GOLD + "'s invitation to join the faction");

            Logger.print(player.getName() + " revoked " + profile.getUsername() + "'s invitation to join " + faction.getName());

            promise.success();
        });
    }

    public void acceptInvite(Player player, String factionName, SimplePromise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);
        final boolean admin = player.hasPermission("factions.admin");

        if (manager.getFactionByPlayer(player.getUniqueId()) != null) {
            promise.failure("You are already in a faction");
            return;
        }

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        if (!faction.isInvited(player.getUniqueId()) && !admin) {
            promise.failure("You have not been invited to this faction");
            return;
        }

        if (faction.isFrozen() && !admin) {
            promise.failure("You can not join this faction while their power is frozen");
            return;
        }

        if (faction.isRaidable() && !admin) {
            promise.failure("You can not join this faction while they are raidable");
            return;
        }

        if (faction.getMembers().size() >= manager.getPlugin().getFactionConfig().getFactionMemberCap() && !admin) {
            promise.failure("Faction is full");
            return;
        }

        if (faction.isReinvited(player.getUniqueId()) && faction.getReinvites() <= 0 && !admin) {
            promise.failure("You have left this faction recently and they are out of re-invites");
            return;
        }

        faction.addMember(player.getUniqueId(), PlayerFaction.FactionRank.MEMBER);
        faction.registerFriendly(player);
        faction.getPendingInvites().remove(player.getUniqueId());

        player.setScoreboard(faction.getScoreboard());

        if (faction.isReinvited(player.getUniqueId())) {
            faction.setReinvites(faction.getReinvites() - 1);

            faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.BLUE + " consumed a re-invite");
            faction.sendMessage(ChatColor.BLUE + "Remaining" + ChatColor.YELLOW + ": " + faction.getReinvites());
        } else {
            faction.getMemberHistory().add(player.getUniqueId());
        }

        faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has " + ChatColor.GREEN + "joined" + ChatColor.GOLD + " the faction");

        Logger.print(player.getName() + " has joined " + faction.getName());

        promise.success();
    }
}
