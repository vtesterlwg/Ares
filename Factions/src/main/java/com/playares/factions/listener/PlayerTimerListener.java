package com.playares.factions.listener;

import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.factions.Factions;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.EnderpearlTimer;
import com.playares.factions.timers.cont.player.HomeTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public final class PlayerTimerListener implements Listener {
    @Getter
    public final Factions plugin;

    public PlayerTimerListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        // TODO: Stuck timer

        final HomeTimer homeTimer = (HomeTimer)profile.getTimer(PlayerTimer.PlayerTimerType.HOME);

        if (homeTimer != null) {
            profile.getTimers().remove(homeTimer);
            player.sendMessage(ChatColor.RED + "Home warp cancelled");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        // TODO: Stuck timer

        final HomeTimer homeTimer = (HomeTimer)profile.getTimer(PlayerTimer.PlayerTimerType.HOME);

        if (homeTimer != null) {
            profile.getTimers().remove(homeTimer);
            player.sendMessage(ChatColor.RED + "Home warp cancelled");
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
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final EnderpearlTimer pearlTimer = (EnderpearlTimer)profile.getTimer(PlayerTimer.PlayerTimerType.ENDERPEARL);

        if (pearlTimer != null) {
            player.sendMessage(ChatColor.RED + "Enderpearls are locked for " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(pearlTimer.getRemaining()) + ChatColor.RED + "s");

            event.setCancelled(true);
            return;
        }

        profile.getTimers().add(new EnderpearlTimer(player.getUniqueId(), plugin.getFactionConfig().getTimerEnderpearl()));
    }
}