package com.riotmc.factions.claims.handler;

import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.claims.ClaimDAO;
import com.riotmc.factions.claims.ClaimManager;
import com.riotmc.factions.claims.DefinedClaim;
import com.riotmc.factions.factions.PlayerFaction;
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

            manager.getClaimRepository().remove(inside);
            ClaimDAO.deleteDefinedClaim(manager.getPlugin().getMongo(), inside);

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (faction.getHome() != null && inside.inside(faction.getHome())) {
                    faction.unsetHome();
                }

                faction.setBalance(faction.getBalance() + inside.getValue());
                faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has unclaimed land. " +
                        ChatColor.GREEN + "$" + String.format("%.2f", inside.getValue()) + ChatColor.GOLD + " has been returned to your faction balance");

                Logger.print(player.getName() + " unclaimed land for " + faction.getName() + " worth $" + inside.getValue());

                promise.success();
            }).run();
        }).run();
    }
}
