package com.playares.arena.listener;

import com.destroystokyo.paper.Title;
import com.playares.arena.Arenas;
import com.playares.arena.event.ArenaPlayerDeathEvent;
import com.playares.arena.match.Match;
import com.playares.arena.match.TeamMatch;
import com.playares.arena.match.UnrankedMatch;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import com.playares.arena.timer.PlayerTimer;
import com.playares.arena.timer.cont.EnderpearlTimer;
import com.playares.commons.base.util.Time;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class CombatListener implements Listener {
    @Getter public final Arenas plugin;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityDamagePrevention(EntityDamageEvent event) {
        final Entity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        final Player player = (Player)entity;
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile != null && !profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Entity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        final Player player = (Player)entity;
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null) {
            return;
        }

        final Match match = plugin.getMatchManager().getMatchByPlayer(profile);

        if (match == null) {
            return;
        }

        if ((player.getHealth() - event.getFinalDamage()) <= 0.0) {
            event.setCancelled(true);

            profile.setStatus(ArenaPlayer.PlayerStatus.INGAME_DEAD);

            if (match instanceof TeamMatch) {
                // TODO: Hide player from both teams

                for (ItemStack contents : player.getInventory().getContents()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), contents);
                }
            }

            player.getInventory().clear();

            final ArenaPlayerDeathEvent deathEvent = new ArenaPlayerDeathEvent(player, player.getKiller(), match);
            Bukkit.getPluginManager().callEvent(deathEvent);
        }
    }

    @EventHandler
    public void onArenaDeath(ArenaPlayerDeathEvent event) {
        final Player player = event.getPlayer();
        final Player killer = event.getKiller();
        final Match match = event.getMatch();

        if (match instanceof UnrankedMatch) {
            final UnrankedMatch unrankedMatch = (UnrankedMatch)match;
            final ArenaPlayer winner = unrankedMatch.getWinner();
            final ChatColor playerColor = (unrankedMatch.getPlayerA().equals(plugin.getPlayerManager().getPlayer(player)) ? ChatColor.YELLOW : ChatColor.AQUA);
            final ChatColor killerColor = (unrankedMatch.getPlayerA().equals(plugin.getPlayerManager().getPlayer(player)) ? ChatColor.AQUA : ChatColor.YELLOW);

            player.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + " has been slain by " + ChatColor.RED + killer.getName());
            killer.sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " has been slain by " + ChatColor.GREEN + killer.getName());

            unrankedMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendMessage(playerColor + player.getName() + ChatColor.GRAY + " has been slain by " + killerColor + killer.getName()));

            if (winner != null) {
                player.sendTitle(new Title("", ChatColor.RED + killer.getName() + ChatColor.GOLD + " Wins!"));
                killer.sendTitle(new Title("", ChatColor.GREEN + "You Win!"));
                unrankedMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendMessage(killerColor + killer.getName() + ChatColor.GOLD + " Wins!"));

                plugin.getMatchManager().getHandler().finish(match);
            }

            return;
        }

        if (match instanceof TeamMatch) {
            final TeamMatch teamMatch = (TeamMatch)match;
            final Team winner = teamMatch.getWinner();

            if (winner != null) {
                plugin.getMatchManager().getHandler().finish(match);
            }
        }
    }

    @EventHandler
    public void onPlayerLaunchProjectile(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity().getShooter();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final Match match = plugin.getMatchManager().getMatchByPlayer(profile);
        final EnderpearlTimer pearlTimer = (EnderpearlTimer)profile.getTimer(PlayerTimer.PlayerTimerType.ENDERPEARL);

        if (pearlTimer != null) {
            player.sendMessage(ChatColor.RED + "Enderpearls are locked for " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(pearlTimer.getRemaining()) + ChatColor.RED + "s");

            event.setCancelled(true);
            return;
        }

        if (match != null) {
            profile.getTimers().add(new EnderpearlTimer(player.getUniqueId(), match.getQueue().getQueueType().getEnderpearlCooldown()));
        } else {
            profile.getTimers().add(new EnderpearlTimer(player.getUniqueId(), 16));
        }
    }
}