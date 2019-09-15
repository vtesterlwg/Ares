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
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.util.Players;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.EntityLightning;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntityWeather;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class CombatListener implements Listener {
    @Getter public final Arenas plugin;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final ArenaPlayer attackerProfile = plugin.getPlayerManager().getPlayer(attacker);
        final ArenaPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked);

        if (attackerProfile == null || attackedProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (!attackerProfile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME) || !attackedProfile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            event.setCancelled(true);
            return;
        }

        final Team attackerTeam = plugin.getTeamManager().getTeam(attackerProfile);

        if (attackerTeam != null && attackerTeam.getMembers().contains(attackedProfile)) {
            event.setCancelled(true);
        }
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

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null) {
            return;
        }

        final Match match = plugin.getMatchManager().getMatchByPlayer(profile);

        if (match == null) {
            return;
        }

        profile.setStatus(ArenaPlayer.PlayerStatus.INGAME_DEAD);
        profile.getActiveReport().pullInventory();

        if (match instanceof TeamMatch) {
            for (ItemStack contents : player.getInventory().getContents()) {
                player.getWorld().dropItemNaturally(player.getLocation(), contents);
            }
        }

        final ArenaPlayerDeathEvent deathEvent = new ArenaPlayerDeathEvent(player, null, match);
        Bukkit.getPluginManager().callEvent(deathEvent);
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
            profile.getActiveReport().pullInventory();

            if (match instanceof TeamMatch) {
                final TeamMatch teamMatch = (TeamMatch)match;

                player.setGameMode(GameMode.CREATIVE);

                teamMatch.getAlivePlayers().forEach(alive -> alive.getPlayer().hidePlayer(plugin, player));
                Bukkit.getOnlinePlayers().forEach(online -> player.showPlayer(plugin, online));

                for (ItemStack contents : player.getInventory().getContents()) {
                    if (contents == null) {
                        continue;
                    }

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
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        // Reset slain player to full health
        Players.resetHealth(player);

        // Lighting effect
        match.getPlayers().forEach(lightingViewer -> {
            ((CraftPlayer)lightingViewer.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityWeather(new EntityLightning(((CraftPlayer)player).getHandle().getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), false)));
            Players.playSound(lightingViewer.getPlayer(), Sound.ENTITY_LIGHTNING_THUNDER);
        });

        if (match instanceof UnrankedMatch) {
            final UnrankedMatch unrankedMatch = (UnrankedMatch)match;
            final ArenaPlayer winner = unrankedMatch.getWinner();
            final ChatColor playerColor = (unrankedMatch.getPlayerA().equals(plugin.getPlayerManager().getPlayer(player)) ? ChatColor.YELLOW : ChatColor.AQUA);
            final ChatColor killerColor = (unrankedMatch.getPlayerA().equals(plugin.getPlayerManager().getPlayer(player)) ? ChatColor.AQUA : ChatColor.YELLOW);

            if (killer != null) {

                // Send slain message
                player.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + " has been slain by " + ChatColor.RED + killer.getName());
                killer.sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " has been slain by " + ChatColor.GREEN + killer.getName());
                unrankedMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendMessage(playerColor + player.getName() + ChatColor.GRAY + " has been slain by " + killerColor + killer.getName()));

                // There is a winner, end the match
                if (winner != null) {
                    player.sendTitle(new Title("", ChatColor.RED + killer.getName() + ChatColor.GOLD + " Wins!"));
                    killer.sendTitle(new Title("", ChatColor.GREEN + "You Win!"));

                    unrankedMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendTitle(new Title("", killerColor + killer.getName() + ChatColor.GOLD + " Wins!")));

                    plugin.getMatchManager().getHandler().finish(match);
                }
            }

            else {
                // Send slain message w/o a killer
                // This can occur if the player suicides by enderpearl or burning etc
                player.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + " died");
                unrankedMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendMessage(playerColor + player.getName() + ChatColor.GRAY + " died"));

                if (unrankedMatch.getPlayerA().getUniqueId().equals(player.getUniqueId())) {
                    // Sending slain message to the killer
                    unrankedMatch.getPlayerB().getPlayer().sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " died");

                    // There is a winner, end the match
                    if (winner != null) {
                        player.sendTitle(new Title("", ChatColor.RED + unrankedMatch.getPlayerB().getUsername() + ChatColor.GOLD + " Wins!"));
                        unrankedMatch.getPlayerB().getPlayer().sendTitle(new Title("", ChatColor.GREEN + "You Win!"));
                        unrankedMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendTitle(new Title("", killerColor + unrankedMatch.getPlayerB().getUsername() + ChatColor.GOLD + " Wins!")));

                        plugin.getMatchManager().getHandler().finish(match);
                    }
                }

                else {

                    unrankedMatch.getPlayerA().getPlayer().sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " died");

                    if (winner != null) {
                        player.sendTitle(new Title("", ChatColor.RED + unrankedMatch.getPlayerA().getUsername() + ChatColor.GOLD + " Wins!"));

                        unrankedMatch.getPlayerA().getPlayer().sendTitle(new Title("", ChatColor.GREEN + "You Win!"));
                        unrankedMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendTitle(new Title("", killerColor + unrankedMatch.getPlayerA().getUsername() + ChatColor.GOLD + " Wins!")));

                        plugin.getMatchManager().getHandler().finish(match);
                    }
                }
            }
        }

        else if (match instanceof TeamMatch) {
            final TeamMatch teamMatch = (TeamMatch)match;
            final ChatColor playerColor = (teamMatch.getTeamA().getMembers().contains(plugin.getPlayerManager().getPlayer(player)) ? ChatColor.YELLOW : ChatColor.AQUA);
            final ChatColor killerColor = (teamMatch.getTeamA().getMembers().contains(plugin.getPlayerManager().getPlayer(player)) ? ChatColor.AQUA : ChatColor.YELLOW);
            final Team winner = teamMatch.getWinner();
            final Team slainTeam = (teamMatch.getTeamA().getMembers().contains(plugin.getPlayerManager().getPlayer(player)) ? teamMatch.getTeamA() : teamMatch.getTeamB());
            final Team killerTeam = (teamMatch.getTeamA().getMembers().contains(plugin.getPlayerManager().getPlayer(player)) ? teamMatch.getTeamB() : teamMatch.getTeamA());

            if (killer != null) {
                slainTeam.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + " has been slain by " + ChatColor.RED + killer.getName());
                killerTeam.sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " has been slain by " + ChatColor.GREEN + killer.getName());
                teamMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendMessage(playerColor + player.getName() + ChatColor.GRAY + " has been slain by " + killerColor + killer.getName()));

                if (winner != null) {
                    slainTeam.sendTitle(new Title("", ChatColor.RED + "Team " + killer.getName() + ChatColor.GOLD + " Wins!"));
                    killerTeam.sendTitle(new Title("", ChatColor.GREEN + "You Win!"));

                    teamMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendTitle(new Title("", killerColor + killer.getName() + ChatColor.GOLD + " Wins!")));

                    plugin.getMatchManager().getHandler().finish(teamMatch);
                }
            }

            else {
                slainTeam.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + " died");
                teamMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendMessage(playerColor + player.getName() + ChatColor.GRAY + " died"));

                if (teamMatch.getTeamA().getMembers().contains(profile)) {
                    teamMatch.getTeamA().sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + " died");
                    teamMatch.getTeamB().sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " died");

                    if (winner != null) {
                        teamMatch.getTeamA().sendTitle(new Title("", ChatColor.RED + "Team " + teamMatch.getTeamB().getLeader().getUsername() + ChatColor.GOLD + " Wins!"));
                        teamMatch.getTeamB().sendTitle(new Title("", ChatColor.GREEN + "You Win!"));
                        teamMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendTitle(new Title("", killerColor + "Team " + teamMatch.getTeamB().getLeader().getUsername() + ChatColor.GOLD + " Wins!")));

                        plugin.getMatchManager().getHandler().finish(teamMatch);
                    }
                }

                else {
                    teamMatch.getTeamB().sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GRAY + " died");
                    teamMatch.getTeamA().sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " died");

                    if (winner != null) {
                        teamMatch.getTeamB().sendTitle(new Title("", ChatColor.RED + "Team " + teamMatch.getTeamA().getLeader().getUsername() + ChatColor.GOLD + " Wins!"));
                        teamMatch.getTeamA().sendTitle(new Title("", ChatColor.GREEN + "You Win!"));
                        teamMatch.getSpectators().forEach(spectator -> spectator.getPlayer().sendTitle(new Title("", killerColor + "Team " + teamMatch.getTeamA().getLeader().getUsername() + ChatColor.GOLD + " Wins!")));

                        plugin.getMatchManager().getHandler().finish(teamMatch);
                    }
                }
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