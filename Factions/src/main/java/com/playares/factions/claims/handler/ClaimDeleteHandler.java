package com.playares.factions.claims.handler;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.claims.dao.ClaimDAO;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.manager.ClaimManager;
import com.playares.factions.claims.subclaims.dao.SubclaimDAO;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class ClaimDeleteHandler {
    @Getter
    public final ClaimManager manager;

    public ClaimDeleteHandler(ClaimManager manager) {
        this.manager = manager;
    }

    public void unclaim(Player player, SimplePromise promise) {
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getFactionByPlayer(player.getUniqueId());
        final DefinedClaim inside = manager.getClaimAt(new PLocatable(player));
        final boolean admin = player.hasPermission("factions.admin");

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        final List<DefinedClaim> claims = getManager().getClaimsByOwner(faction);

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !admin) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        if (claims.isEmpty()) {
            promise.failure("Your faction does not have any claims");
            return;
        }

        if (inside == null) {
            promise.failure("You are not standing in claimed land");
            return;
        }

        if (!inside.getOwnerId().equals(faction.getUniqueId())) {
            promise.failure("Your faction does not own this claim");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            if (claims.size() >= 3) {
                for (DefinedClaim a : claims.stream().filter(claimA -> !claimA.getUniqueId().equals(inside.getUniqueId())).collect(Collectors.toList())) {
                    boolean valid = false;

                    for (BLocatable perimeter : a.getPerimeter(64)) {
                        for (DefinedClaim b : claims.stream().filter(claimB -> !claimB.getUniqueId().equals(inside.getUniqueId()) && !claimB.getUniqueId().equals(a.getUniqueId())).collect(Collectors.toList())) {
                            if (b.touching(perimeter)) {
                                valid = true;
                                break;
                            }
                        }
                    }

                    if (!valid) {
                        new Scheduler(manager.getPlugin()).sync(() -> promise.failure("Claims would no longer be connected if this land was unclaimed")).run();
                        return;
                    }
                }
            }

            final List<Subclaim> subclaimsInside = getManager().getPlugin().getSubclaimManager().getSubclaimsInside(inside);

            subclaimsInside.forEach(subclaim -> {
                getManager().getPlugin().getSubclaimManager().getSubclaimRepository().removeAll(subclaimsInside);
                SubclaimDAO.deleteSubclaims(getManager().getPlugin().getMongo(), subclaimsInside);
            });

            manager.getClaimRepository().remove(inside);
            ClaimDAO.deleteDefinedClaim(manager.getPlugin().getMongo(), inside);

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (faction.getHome() != null && inside.inside(faction.getHome())) {
                    faction.unsetHome();
                }

                faction.setBalance(faction.getBalance() + (inside.getValue() * manager.getPlugin().getFactionConfig().getRefundedPercent()));

                if (!subclaimsInside.isEmpty()) {
                    faction.sendMessage(ChatColor.YELLOW + "" + subclaimsInside.size() + ChatColor.GOLD + " subclaims have been removed from unclaiming this land");
                }

                faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has unclaimed land. " +
                        ChatColor.GREEN + "$" + String.format("%.2f", (inside.getValue() * manager.getPlugin().getFactionConfig().getRefundedPercent())) + ChatColor.GOLD + " has been returned to your faction balance");

                Logger.print(player.getName() + " unclaimed land for " + faction.getName() + " worth $" + inside.getValue());

                promise.success();
            }).run();
        }).run();
    }

    public void unclaimAll(Player player, SimplePromise promise) {
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getFactionByPlayer(player.getUniqueId());
        final boolean admin = player.hasPermission("factions.admin");

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        final List<DefinedClaim> claims = getManager().getClaimsByOwner(faction);
        final List<Subclaim> subclaims = getManager().getPlugin().getSubclaimManager().getSubclaimsByOwner(faction);

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !admin) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        if (claims.isEmpty()) {
            promise.failure("Your faction does not have any claims");
            return;
        }

        manager.getClaimRepository().removeAll(claims);
        manager.getPlugin().getSubclaimManager().getSubclaimRepository().removeAll(subclaims);

        new Scheduler(manager.getPlugin()).async(() -> {
            double refunded = 0.0;

            ClaimDAO.deleteDefinedClaims(manager.getPlugin().getMongo(), claims);
            SubclaimDAO.deleteSubclaims(manager.getPlugin().getMongo(), subclaims);

            for (DefinedClaim claim : claims) {
                refunded += claim.getValue();
            }

            faction.setBalance(faction.getBalance() + (refunded * manager.getPlugin().getFactionConfig().getRefundedPercent()));

            final double refundedFinal = refunded;

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (faction.getHome() != null) {
                    faction.unsetHome();
                }

                faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has unclaimed all of your factions land. " +
                        ChatColor.GREEN + "$" + String.format("%.2f", (refundedFinal * manager.getPlugin().getFactionConfig().getRefundedPercent())) + ChatColor.GOLD + " has been returned to your faction balance");

                Logger.print(player.getName() + " unclaimed all land for " + faction.getName() + " worth $" + refundedFinal);

                promise.success();
            }).run();
        }).run();
    }

    public void unclaimFor(Player player, String name, SimplePromise promise) {
        final Faction faction = manager.getPlugin().getFactionManager().getFactionByName(name);
        final DefinedClaim inside = manager.getClaimAt(new PLocatable(player));

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        if (inside == null) {
            promise.failure("You are not standing inside a claim");
            return;
        }

        if (!inside.getOwnerId().equals(faction.getUniqueId())) {
            promise.failure("This claim does not belong to " + faction.getName());
            return;
        }

        final List<DefinedClaim> claims = getManager().getClaimsByOwner(faction);

        new Scheduler(manager.getPlugin()).async(() -> {
            if (claims.size() >= 3) {
                for (DefinedClaim a : claims.stream().filter(claimA -> !claimA.getUniqueId().equals(inside.getUniqueId())).collect(Collectors.toList())) {
                    boolean valid = false;

                    for (BLocatable perimeter : a.getPerimeter(64)) {
                        for (DefinedClaim b : claims.stream().filter(claimB -> !claimB.getUniqueId().equals(inside.getUniqueId()) && !claimB.getUniqueId().equals(a.getUniqueId())).collect(Collectors.toList())) {
                            if (b.touching(perimeter)) {
                                valid = true;
                                break;
                            }
                        }
                    }

                    if (!valid) {
                        new Scheduler(manager.getPlugin()).sync(() -> promise.failure("Claims would no longer be connected if this land was unclaimed"));
                        return;
                    }

                    if (faction instanceof PlayerFaction) {
                        final PlayerFaction playerFaction = (PlayerFaction)faction;
                        final List<Subclaim> subclaims = getManager().getPlugin().getSubclaimManager().getSubclaimsInside(inside);

                        manager.getPlugin().getSubclaimManager().getSubclaimRepository().removeAll(subclaims);
                        SubclaimDAO.deleteSubclaims(getManager().getPlugin().getMongo(), subclaims);

                        new Scheduler(getManager().getPlugin()).sync(() -> playerFaction.sendMessage(ChatColor.GOLD + "" + subclaims.size() + ChatColor.YELLOW + " subclaims have been removed by unclaiming this land")).run();
                    }

                    manager.getClaimRepository().remove(inside);
                    ClaimDAO.deleteDefinedClaim(manager.getPlugin().getMongo(), inside);

                    new Scheduler(manager.getPlugin()).sync(() -> {
                        if (faction instanceof PlayerFaction) {
                            final PlayerFaction playerFaction = (PlayerFaction)faction;

                            if (playerFaction.getHome() != null && inside.inside(playerFaction.getHome())) {
                                playerFaction.unsetHome();
                            }

                            playerFaction.setBalance(playerFaction.getBalance() + (inside.getValue() * manager.getPlugin().getFactionConfig().getRefundedPercent()));

                            playerFaction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has unclaimed land. " +
                                    ChatColor.GREEN + "$" + String.format("%.2f", (inside.getValue() * manager.getPlugin().getFactionConfig().getRefundedPercent())) + ChatColor.GOLD + " has been returned to your faction balance");
                        }

                        Logger.print(player.getName() + " unclaimed land for " + faction.getName());

                        promise.success();
                    }).run();
                }
            }
        }).run();
    }

    public void unclaimAllFor(Player player, String name, SimplePromise promise) {
        final String username = player.getName();
        final Faction faction = getManager().getPlugin().getFactionManager().getFactionByName(name);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        final List<DefinedClaim> claims = getManager().getClaimsByOwner(faction);

        if (claims.isEmpty()) {
            promise.failure("This faction does not have any claims");
            return;
        }

        new Scheduler(getManager().getPlugin()).async(() -> {
            if (faction instanceof PlayerFaction) {
                final PlayerFaction playerFaction = (PlayerFaction)faction;
                final List<Subclaim> subclaims = getManager().getPlugin().getSubclaimManager().getSubclaimsByOwner(playerFaction);
                double refunded = 0.0;

                if (!subclaims.isEmpty()) {
                    getManager().getPlugin().getSubclaimManager().getSubclaimRepository().removeAll(subclaims);
                    SubclaimDAO.deleteSubclaims(getManager().getPlugin().getMongo(), subclaims);
                }

                for (DefinedClaim claim : claims) {
                    refunded += claim.getValue();
                }

                playerFaction.setBalance(playerFaction.getBalance() + (refunded * manager.getPlugin().getFactionConfig().getRefundedPercent()));

                final double refundedFinal = refunded;

                new Scheduler(getManager().getPlugin()).sync(() -> playerFaction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has unclaimed all of your factions land. " +
                        ChatColor.GREEN + "$" + String.format("%.2f", (refundedFinal * manager.getPlugin().getFactionConfig().getRefundedPercent())) + ChatColor.GOLD + " has been returned to your faction balance")).run();
            }

            ClaimDAO.deleteDefinedClaims(getManager().getPlugin().getMongo(), claims);

            Logger.print(username + " unclaimed all land for " + faction.getName());

            new Scheduler(getManager().getPlugin()).sync(promise::success).run();
        }).run();
    }
}