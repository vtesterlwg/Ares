package com.playares.factions.listener;

import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.factions.Factions;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.event.PlayerChangeClaimEvent;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.factions.ServerFaction;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ClaimListener implements Listener {
    @Getter
    public final Factions plugin;

    public ClaimListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final DefinedClaim expectedClaim = profile.getCurrentClaim();
        final DefinedClaim predictedClaim = plugin.getClaimManager().getClaimAt(new PLocatable(
                event.getTo().getWorld().getName(),
                event.getTo().getX(),
                event.getTo().getY(),
                event.getTo().getZ(),
                event.getTo().getYaw(),
                event.getTo().getPitch()));

        if (expectedClaim == predictedClaim) {
            return;
        }

        final PlayerChangeClaimEvent changeClaimEvent = new PlayerChangeClaimEvent(player, expectedClaim, predictedClaim);
        Bukkit.getPluginManager().callEvent(changeClaimEvent);

        if (changeClaimEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        profile.setCurrentClaim(predictedClaim);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerChangeClaim(PlayerChangeClaimEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final DefinedClaim to = event.getTo();

        if (to != null) {
            final Faction toFaction = plugin.getFactionManager().getFactionById(to.getOwnerId());

            if (toFaction == null) {
                return;
            }

            if (toFaction instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)toFaction;

                if (profile.hasTimer(PlayerTimer.PlayerTimerType.COMBAT) && sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while combat-tagged");
                    event.setCancelled(true);
                    return;
                }

                if (profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION) && sf.getFlag().equals(ServerFaction.FactionFlag.EVENT)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while you have PvP Protection");
                    event.setCancelled(true);
                }
            } else if (toFaction instanceof PlayerFaction) {
                if (profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while you have PvP Protection");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerClaimChangeNotification(PlayerChangeClaimEvent event) {
        final Player player = event.getPlayer();
        final DefinedClaim from = event.getFrom();
        final DefinedClaim to = event.getTo();

        if (event.isCancelled()) {
            return;
        }

        if (from != null && to != null && from.getOwnerId().equals(to.getOwnerId())) {
            return;
        }

        if (from != null) {
            final Faction owner = plugin.getFactionManager().getFactionById(from.getOwnerId());

            if (owner != null) {
                if (owner instanceof ServerFaction) {
                    final ServerFaction sf = (ServerFaction)owner;
                    player.sendMessage(ChatColor.GOLD + "Now Leaving" + ChatColor.YELLOW + ": " + ChatColor.RESET + sf.getDisplayName());
                } else if (owner instanceof PlayerFaction) {
                    final PlayerFaction pf = (PlayerFaction)owner;
                    final ChatColor color = (pf.getMember(player.getUniqueId()) != null ? ChatColor.GREEN : ChatColor.RED);
                    player.sendMessage(ChatColor.GOLD + "Now Leaving" + ChatColor.YELLOW + ": " + color + pf.getName());
                }
            }
        }

        if (to != null) {
            final Faction owner = plugin.getFactionManager().getFactionById(to.getOwnerId());

            if (owner != null) {
                if (owner instanceof ServerFaction) {
                    final ServerFaction sf = (ServerFaction)owner;
                    player.sendMessage(ChatColor.GOLD + "Now Entering" + ChatColor.YELLOW + ": " + ChatColor.RESET + sf.getDisplayName());
                } else if (owner instanceof PlayerFaction) {
                    final PlayerFaction pf = (PlayerFaction)owner;
                    final ChatColor color = (pf.getMember(player.getUniqueId()) != null ? ChatColor.GREEN : ChatColor.RED);
                    player.sendMessage(ChatColor.GOLD + "Now Entering" + ChatColor.YELLOW + ": " + color + pf.getName());
                }
            }
        }
    }
}