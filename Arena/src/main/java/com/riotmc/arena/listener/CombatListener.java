package com.riotmc.arena.listener;

import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.commons.bukkit.util.Worlds;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.aftermatch.PlayerReport;
import com.riotmc.arena.match.Match;
import com.riotmc.arena.match.MatchStatus;
import com.riotmc.arena.match.cont.DuelMatch;
import com.riotmc.arena.match.cont.TeamMatch;
import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.arena.player.PlayerStatus;
import com.riotmc.arena.team.Team;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nonnull;

public final class CombatListener implements Listener {
    @Nonnull @Getter
    public final Arenas plugin;

    public CombatListener(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        final Player damager = event.getDamager();
        final Player damaged = event.getDamaged();

        if (damager.getUniqueId().equals(damaged.getUniqueId())) {
            return;
        }

        final ArenaPlayer arenaPlayerA = plugin.getPlayerManager().getPlayer(damager.getUniqueId());

        if (
                arenaPlayerA == null ||
                arenaPlayerA.getMatch() == null ||
                !arenaPlayerA.getMatch().getStatus().equals(MatchStatus.IN_PROGRESS) ||
                arenaPlayerA.getMatch().getSpectators().contains(arenaPlayerA) ||
                !arenaPlayerA.getStatus().equals(PlayerStatus.INGAME)) {

            event.setCancelled(true);
            return;

        }

        if (arenaPlayerA.getMatch() instanceof DuelMatch) {
            final DuelMatch duel = (DuelMatch)arenaPlayerA.getMatch();
            final ArenaPlayer arenaPlayerB = plugin.getPlayerManager().getPlayer(damaged.getUniqueId());

            if (arenaPlayerB == null) {
                event.setCancelled(true);
                return;
            }

            if (!duel.getOpponents().contains(arenaPlayerB)) {
                event.setCancelled(true);
                return;
            }

            arenaPlayerA.addHit();
            arenaPlayerA.addDamage(event.getDamage());

            if (event.getType().equals(PlayerDamagePlayerEvent.DamageType.PROJECTILE)) {
                final double distance = new PLocatable(damager).distance(new PLocatable(damaged));

                arenaPlayerA.addArrowHit();

                if (distance > arenaPlayerA.getLongestShot()) {
                    arenaPlayerA.setLongestShot(distance);
                }
            }

            return;
        }

        if (arenaPlayerA.getMatch() instanceof TeamMatch) {
            final TeamMatch teamfight = (TeamMatch)arenaPlayerA.getMatch();
            final ArenaPlayer arenaPlayerB = plugin.getPlayerManager().getPlayer(damaged.getUniqueId());

            if (arenaPlayerB == null) {
                event.setCancelled(true);
                return;
            }

            final Team teamA = teamfight.getTeam(arenaPlayerA);
            final Team teamB = teamfight.getTeam(arenaPlayerB);

            if (teamA == null || teamA.getMembers().contains(arenaPlayerB)) {
                event.setCancelled(true);
                return;
            }

            if (teamB == null || !teamfight.getOpponents().contains(teamB)) {
                event.setCancelled(true);
                return;
            }

            teamA.addHit();
            teamA.addDamage(event.getDamage());
            arenaPlayerA.addHit();
            arenaPlayerA.addDamage(event.getDamage());

            if (event.getType().equals(PlayerDamagePlayerEvent.DamageType.PROJECTILE)) {
                final double distance = new PLocatable(damager).distance(new PLocatable(damaged));

                arenaPlayerA.addArrowHit();

                if (distance > arenaPlayerA.getLongestShot()) {
                    arenaPlayerA.setLongestShot(distance);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        final Projectile projectile = event.getEntity();
        final ProjectileSource shooter = projectile.getShooter();

        if (!(shooter instanceof Player)) {
            return;
        }

        final Player player = (Player)shooter;
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        if (projectile instanceof EnderPearl) {
            new Scheduler(plugin).sync(() -> player.setCooldown(Material.ENDER_PEARL, (16 * 20))).delay(1L).run();
            return;
        }

        if (projectile instanceof Arrow) {
            profile.addArrowFired();
            return;
        }

        if (projectile instanceof ThrownPotion && profile.getTeam() != null) {
            final ThrownPotion potion = (ThrownPotion)projectile;
            boolean isHP = false;

            for (PotionEffect effect : potion.getEffects()) {
                if (effect.getType().equals(PotionEffectType.HEAL)) {
                    isHP = true;
                    break;
                }
            }

            if (isHP) {
                profile.getTeam().addUsedHealthPotion();
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onDeath(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            return;
        }

        final Match match = profile.getMatch();
        final double damage = event.getFinalDamage();

        if (match == null || !profile.getStatus().equals(PlayerStatus.INGAME)) {
            event.setCancelled(true);
            return;
        }

        if ((player.getHealth() - damage) <= 0.0) {
            final PlayerReport report = new PlayerReport(profile, match.getUniqueId(), (profile.getTeam() != null ? profile.getTeam().getUniqueId() : null), 0.0);
            match.getPlayerReports().add(report);

            Worlds.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE);

            for (ItemStack contents : player.getInventory().getContents()) {
                if (contents == null) {
                    continue;
                }

                player.getWorld().dropItem(player.getLocation(), contents);
            }

            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor == null) {
                    continue;
                }

                player.getWorld().dropItem(player.getLocation(), armor);
            }

            Players.resetHealth(player);
            player.setGameMode(GameMode.CREATIVE);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setVelocity(player.getVelocity().setY(0.6));

            profile.setStatus(PlayerStatus.INGAME_DEAD);

            if (match instanceof DuelMatch) {
                final DuelMatch duel = (DuelMatch)match;
                final ArenaPlayer winner = duel.getWinner();

                if (player.getKiller() != null) {
                    match.sendMessage(duel.getViewers(), ChatColor.GREEN + player.getKiller().getName() + ChatColor.GRAY + " killed " + ChatColor.RED + player.getName());
                } else {
                    match.sendMessage(duel.getViewers(), ChatColor.RED + player.getName() + ChatColor.GRAY + " died");
                }

                plugin.getSpectatorHandler().updateSpectators(profile);

                if (winner != null) {
                    plugin.getArenaHandler().finishArena(match);
                }

                return;
            }

            if (match instanceof TeamMatch) {
                final TeamMatch teamfight = (TeamMatch)match;
                final Team winner = teamfight.getWinner();

                teamfight.getSpectators().forEach(spectator -> {
                    if (spectator.getPlayer() != null) {
                        if (player.getKiller() != null) {
                            spectator.getPlayer().sendMessage(ChatColor.AQUA + player.getKiller().getName() + ChatColor.GRAY + " killed " + ChatColor.AQUA + player.getName());
                        } else {
                            spectator.getPlayer().sendMessage(ChatColor.AQUA + player.getName() + ChatColor.GRAY + " died");
                        }
                    }
                });

                teamfight.getOpponents().forEach(team -> {
                    final ChatColor killedColor;
                    final ChatColor killerColor;

                    if (team.getMembers().contains(profile)) {
                        killedColor = ChatColor.GREEN;
                        killerColor = ChatColor.RED;
                    } else {
                        killedColor = ChatColor.RED;
                        killerColor = ChatColor.GREEN;
                    }

                    if (player.getKiller() != null) {
                        team.sendMessage(killerColor + player.getKiller().getName() + ChatColor.GRAY + " killed " + killedColor + player.getName());
                    } else {
                        team.sendMessage(killedColor + player.getName() + ChatColor.GRAY + " died");
                    }
                });

                plugin.getSpectatorHandler().updateSpectators(profile);

                if (winner != null) {
                    plugin.getArenaHandler().finishArena(match);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final Match match = profile.getMatch();

        if (match == null || !profile.getStatus().equals(PlayerStatus.INGAME)) {
            return;
        }

        profile.setStatus(PlayerStatus.INGAME_DEAD);

        final PlayerReport report = new PlayerReport(profile, match.getUniqueId(), (profile.getTeam() != null ? profile.getTeam().getUniqueId() : null), 0.0);
        match.getPlayerReports().add(report);

        if (match instanceof DuelMatch) {
            final DuelMatch duel = (DuelMatch)match;
            final ArenaPlayer winner = duel.getWinner();

            match.sendMessage(duel.getViewers(), ChatColor.RED + player.getName() + ChatColor.GRAY + " disconnected");

            if (winner == null) {
                for (ItemStack contents : player.getInventory().getContents()) {
                    player.getWorld().dropItem(player.getLocation(), contents);
                }

                for (ItemStack armor : player.getInventory().getArmorContents()) {
                    player.getWorld().dropItem(player.getLocation(), armor);
                }

                return;
            }

            plugin.getArenaHandler().finishArena(match);
            return;
        }

        if (match instanceof TeamMatch) {
            final TeamMatch teamfight = (TeamMatch)match;
            final Team winner = teamfight.getWinner();

            match.sendMessage(teamfight.getViewers(), ChatColor.RED + player.getName() + ChatColor.GRAY + " disconnected");

            if (winner == null) {
                for (ItemStack contents : player.getInventory().getContents()) {
                    player.getWorld().dropItem(player.getLocation(), contents);
                }

                for (ItemStack armor : player.getInventory().getArmorContents()) {
                    player.getWorld().dropItem(player.getLocation(), armor);
                }

                return;
            }

            plugin.getArenaHandler().finishArena(match);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            event.setCancelled(true);
            return;
        }

        if (profile.getMatch() == null || !profile.getStatus().equals(PlayerStatus.INGAME)) {
            event.setCancelled(true);
        }
    }
}