package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.claims.ClaimDAO;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.factions.FactionDAO;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.players.FactionPlayer;
import com.playares.services.profiles.ProfileService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public final class FactionDisbandHandler {
    @Getter
    public final FactionManager manager;

    public FactionDisbandHandler(FactionManager manager) {
        this.manager = manager;
    }

    public void disband(Player player, SimplePromise promise) {
        final FactionPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean admin = player.hasPermission("factions.admin");

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (!faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.LEADER) && !admin) {
            promise.failure("You must be the faction leader to perform this action");
            return;
        }

        if (faction.isRaidable() && !admin) {
            promise.failure("You can not disband your faction while raid-able");
            return;
        }

        final List<DefinedClaim> claims = manager.getPlugin().getClaimManager().getClaimsByOwner(faction);

        claims.forEach(claim -> faction.setBalance(faction.getBalance() + claim.getValue()));

        faction.getOnlineMembers().forEach(member -> {
            final Player bukkitMember = Bukkit.getPlayer(member.getUniqueId());
            final FactionPlayer memberProfile = manager.getPlugin().getPlayerManager().getPlayer(member.getUniqueId());

            if (memberProfile != null) {
                memberProfile.setFaction(null);
            }

            if (bukkitMember != null) {
                faction.unregister(bukkitMember);
                bukkitMember.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        });

        new Scheduler(manager.getPlugin()).async(() -> {
            claims.forEach(claim -> ClaimDAO.deleteDefinedClaim(manager.getPlugin().getMongo(), claim));
            FactionDAO.deleteFaction(manager.getPlugin().getMongo(), faction);

            new Scheduler(manager.getPlugin()).sync(() -> {
                faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.RED + " disbanded the faction");

                profile.setBalance(profile.getBalance() + faction.getBalance());

                manager.getFactionRepository().remove(faction);

                Logger.print(player.getName() + " disbanded " + faction.getName());

                promise.success();
            }).run();
        }).run();
    }

    public void leave(Player player, SimplePromise promise) {
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final DefinedClaim inside = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));
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

        if (inside != null && inside.getOwnerId().equals(faction.getUniqueId())) {
            promise.failure("You must leave " + faction.getName() + "'s claims before leaving the faction");
            return;
        }

        faction.getMembers().remove(faction.getMember(player.getUniqueId()));
        faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has " + ChatColor.RED + "left" + ChatColor.GOLD + " the faction");

        Logger.print(player.getName() + " has left " + faction.getName());

        promise.success();
    }

    public void kick(Player player, String name, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)manager.getPlugin().getService(ProfileService.class);
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean mod = player.hasPermission("factions.mod");

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        final PlayerFaction.FactionProfile kicker = faction.getMember(player.getUniqueId());

        profileService.getProfile(name, kickedProfile -> {
            if (kickedProfile == null) {
                promise.failure("Player not found");
                return;
            }

            final PlayerFaction.FactionProfile kicked = faction.getMember(kickedProfile.getUniqueId());

            if (kicked == null) {
                promise.failure(kickedProfile.getUsername() + " is not in your faction");
                return;
            }

            if (!kicker.getRank().isHigher(kicked.getRank()) && !mod) {
                promise.failure("Can not kick " + kickedProfile.getUsername() + " because they have the same or a higher ranking than you");
                return;
            }

            if (faction.isRaidable() && !mod) {
                promise.failure("Players can not be kicked while the faction is raid-able");
                return;
            }

            if (faction.isFrozen() && !mod) {
                promise.failure("Players can not be kicked while DTR is frozen - If you believe you are being betrayed contact the staff immediately");
                return;
            }

            if (Bukkit.getPlayer(kickedProfile.getUniqueId()) != null) {
                final Player kickedPlayer = Bukkit.getPlayer(kickedProfile.getUniqueId());

                faction.unregister(kickedPlayer);

                kickedPlayer.sendMessage(ChatColor.RED + "You have been kicked from the faction");
                kickedPlayer.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }

            faction.sendMessage(ChatColor.YELLOW + kickedProfile.getUsername() + ChatColor.GOLD + " has been " + ChatColor.RED + "kicked" + ChatColor.GOLD + " from the faction by " + ChatColor.YELLOW + player.getName());
            faction.getMembers().remove(kicked);

            Logger.print(player.getName() + " kicked " + kickedProfile.getUsername() + " from " + faction.getName());

            promise.success();
        });
    }
}