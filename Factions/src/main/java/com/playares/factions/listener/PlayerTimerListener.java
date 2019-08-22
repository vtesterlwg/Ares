package com.playares.factions.listener;

import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.*;
import com.playares.services.playerclasses.data.Class;
import com.playares.services.playerclasses.event.PlayerClassReadyEvent;
import com.playares.services.playerclasses.event.PlayerClassUnreadyEvent;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public final class PlayerTimerListener implements Listener {
    @Getter public final Factions plugin;

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

        final HomeTimer homeTimer = (HomeTimer)profile.getTimer(PlayerTimer.PlayerTimerType.HOME);
        final StuckTimer stuckTimer = (StuckTimer)profile.getTimer(PlayerTimer.PlayerTimerType.STUCK);
        final LogoutTimer logoutTimer = (LogoutTimer)profile.getTimer(PlayerTimer.PlayerTimerType.LOGOUT);

        if (homeTimer != null) {
            profile.getTimers().remove(homeTimer);
            player.sendMessage(ChatColor.RED + "Home warp cancelled");
        }

        if (stuckTimer != null) {
            profile.getTimers().remove(stuckTimer);
            player.sendMessage(ChatColor.RED + "Stuck warp cancelled");
        }

        if (logoutTimer != null) {
            profile.getTimers().remove(logoutTimer);
            player.sendMessage(ChatColor.RED + "Logout cancelled");
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final HomeTimer homeTimer = (HomeTimer)profile.getTimer(PlayerTimer.PlayerTimerType.HOME);
        final StuckTimer stuckTimer = (StuckTimer)profile.getTimer(PlayerTimer.PlayerTimerType.STUCK);
        final LogoutTimer logoutTimer = (LogoutTimer)profile.getTimer(PlayerTimer.PlayerTimerType.LOGOUT);

        if (homeTimer != null) {
            profile.getTimers().remove(homeTimer);
            player.sendMessage(ChatColor.RED + "Home warp cancelled");
        }

        if (stuckTimer != null) {
            profile.getTimers().remove(stuckTimer);
            player.sendMessage(ChatColor.RED + "Stuck warp cancelled");
        }

        if (logoutTimer != null) {
            profile.getTimers().remove(logoutTimer);
            player.sendMessage(ChatColor.RED + "Logout cancelled");
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

    @EventHandler
    public void onPlayerRevive(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player) || event.isCancelled()) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        TotemTimer timer = (TotemTimer)profile.getTimer(PlayerTimer.PlayerTimerType.TOTEM);

        if (timer != null) {
            player.sendMessage(ChatColor.RED + "Totems are locked for " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(timer.getRemaining()) + ChatColor.RED + "s");

            event.setCancelled(true);
            return;
        }

        timer = new TotemTimer(player.getUniqueId(), plugin.getFactionConfig().getTimerTotem());
        profile.getTimers().add(timer);
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (event.isCancelled()) {
            return;
        }

        if (item == null || !(item.getType().equals(Material.GOLDEN_APPLE))) {
            return;
        }

        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        if (item.getDurability() == (short)0) {
            CrappleTimer timer = (CrappleTimer)profile.getTimer(PlayerTimer.PlayerTimerType.CRAPPLE);

            if (timer != null) {
                player.sendMessage(ChatColor.RED + "Crapples are locked for " +
                        ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(timer.getRemaining()) + ChatColor.RED + "s");

                event.setCancelled(true);
                return;
            }

            timer = new CrappleTimer(player.getUniqueId(), plugin.getFactionConfig().getTimerCrapple());
            profile.getTimers().add(timer);
            return;
        }

        if (item.getDurability() == (short)1) {
            GappleTimer timer = (GappleTimer)profile.getTimer(PlayerTimer.PlayerTimerType.GAPPLE);

            if (timer != null) {
                player.sendMessage(ChatColor.RED + "Gapples are locked for " +
                        ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(timer.getRemaining()) + ChatColor.RED + "s");

                event.setCancelled(true);
                return;
            }

            timer = new GappleTimer(player.getUniqueId(), plugin.getFactionConfig().getTimerGapple());
            profile.getTimers().add(timer);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (item == null || !(item.getType().equals(Material.TOTEM) || (item.getType().equals(Material.GOLDEN_APPLE) && item.getDurability() == (short)1))) {
            return;
        }

        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        if (item.getType().equals(Material.TOTEM) && profile.getTimer(PlayerTimer.PlayerTimerType.TOTEM) != null) {
            player.sendMessage(ChatColor.RED + "Totems are locked for " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(profile.getTimer(PlayerTimer.PlayerTimerType.TOTEM).getRemaining()) + ChatColor.RED + "s");

            event.setCancelled(true);
            return;
        }

        if (item.getType().equals(Material.GOLDEN_APPLE) && item.getDurability() == (short)1 && profile.getTimer(PlayerTimer.PlayerTimerType.GAPPLE) != null) {
            player.sendMessage(ChatColor.RED + "Gapples are locked for " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(profile.getTimer(PlayerTimer.PlayerTimerType.GAPPLE).getRemaining()) + ChatColor.RED + "s");

            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onClassReady(PlayerClassReadyEvent event) {
        final Player player = event.getPlayer();
        final Class playerClass = event.getPlayerClass();

        if (event.isCancelled()) {
            return;
        }

        final FactionPlayer profile = getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            event.setCancelled(true);
            return;
        }

        profile.addTimer(new ClassTimer(player.getUniqueId(), playerClass, playerClass.getWarmup()));
        player.sendMessage(ChatColor.BLUE + playerClass.getName() + ChatColor.GOLD + " will be ready in " + ChatColor.YELLOW + playerClass.getWarmup() + " seconds");
    }

    @EventHandler
    public void onClassUnready(PlayerClassUnreadyEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            Logger.error("Failed to obtain profile for " + player.getName() + " while trying to remove their class!");
            return;
        }

        final ClassTimer classTimer = (ClassTimer)profile.getTimer(PlayerTimer.PlayerTimerType.CLASS);

        if (classTimer != null) {
            profile.getTimers().remove(classTimer);
            player.sendMessage(ChatColor.RED + "Class warm-up cancelled");
        }
    }
}