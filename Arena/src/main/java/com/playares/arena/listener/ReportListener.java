package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.event.ArenaPlayerDeathEvent;
import com.playares.arena.match.Match;
import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@AllArgsConstructor
public final class ReportListener implements Listener {
    @Getter public final Arenas plugin;

    @EventHandler
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        final ArenaPlayer attacker = plugin.getPlayerManager().getPlayer(event.getDamager());
        final double damage = event.getDamage();

        if (attacker.getActiveReport() != null) {
            if (event.getType().equals(PlayerDamagePlayerEvent.DamageType.PHYSICAL)) {
                attacker.getActiveReport().addSwordHit();
            }

            if (event.getType().equals(PlayerDamagePlayerEvent.DamageType.PROJECTILE)) {
                attacker.getActiveReport().addArrowHit();
            }

            attacker.getActiveReport().addDamage(damage);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        final Projectile projectile = event.getEntity();

        if (!(projectile instanceof Arrow) || !(projectile.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)projectile.getShooter();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile != null && profile.getActiveReport() != null) {
            profile.getActiveReport().addArrowFired();
        }
    }

    @EventHandler
    public void onArenaPlayerDeath(ArenaPlayerDeathEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);
        final Match match = event.getMatch();

        if (profile != null && profile.getActiveReport() != null) {
            profile.getActiveReport().setHealth(0.0);
            profile.getActiveReport().setFood(player.getFoodLevel());

            match.getPlayerReports().add(profile.getActiveReport());
        }
    }
}