package com.playares.factions.claims.subclaims.handler;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.claims.subclaims.dao.SubclaimDAO;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.claims.subclaims.manager.SubclaimManager;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class SubclaimDeletionHandler {
    @Getter public final SubclaimManager manager;

    public void deleteSubclaim(Player player, Subclaim subclaim, SimplePromise promise) {
        getManager().getSubclaimRepository().remove(subclaim);

        new Scheduler(getManager().getPlugin()).async(() -> {
            SubclaimDAO.deleteSubclaim(getManager().getPlugin().getMongo(), subclaim);

            new Scheduler(getManager().getPlugin()).sync(() -> {
                final PlayerFaction faction = subclaim.getFaction();

                if (faction != null) {
                    for (PlayerFaction.FactionProfile member : faction.getOnlineMembers()) {
                        if (subclaim.canAccess(member.getUniqueId(), member.getRank())) {
                            final Player factionMember = Bukkit.getPlayer(member.getUniqueId());

                            if (factionMember != null) {
                                factionMember.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.YELLOW + " deleted a subclaim you had access to");
                            }
                        }
                    }
                }

                promise.success();
            }).run();
        }).run();
    }
}