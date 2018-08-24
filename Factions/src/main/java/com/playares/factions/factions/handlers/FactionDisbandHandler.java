package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import com.playares.services.profiles.ProfileService;
import lombok.Getter;
import org.bukkit.Bukkit;
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