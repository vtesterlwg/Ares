package com.playares.civilization.addons.prisonpearls.listener;

import com.playares.civilization.addons.prisonpearls.PrisonPearlAddon;
import com.playares.civilization.addons.prisonpearls.data.PrisonPearl;
import com.playares.civilization.util.CivUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PrisonPearlListener implements Listener {
    @Getter public final PrisonPearlAddon addon;

    public PrisonPearlListener(PrisonPearlAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final Player killer = player.getKiller();

        if (killer == null || player.getUniqueId().equals(killer.getUniqueId())) {
            return;
        }

        final PrisonPearl prisonPearl = addon.getPrisonPearlManager().getPrisonPearlHandler().generatePrisonPearl(killer, player, event.getDeathMessage());

        if (prisonPearl == null) {
            return;
        }

        // Spawn pearl and update location
        if (killer.getInventory().firstEmpty() == -1) {
            killer.getLocation().getWorld().dropItemNaturally(killer.getLocation().add(0, 1.0, 0), prisonPearl.getItem());
            prisonPearl.updateLocation(killer.getLocation());
        } else {
            killer.getInventory().addItem(prisonPearl.getItem());
            prisonPearl.updateLocation(killer);
        }

        addon.getPrisonPearlManager().getPearlRepository().add(prisonPearl);

        CivUtils.sendMessage(Bukkit.getOnlinePlayers(), event.getDeathMessage(), player.getLocation(), addon.getAddonManager().getPlugin().getCivConfig().getDeathMessageRange());

        CivUtils.sendMessage(
                Bukkit.getOnlinePlayers(),
                ChatColor.DARK_PURPLE + player.getName() + ChatColor.RED + " has been imprisoned by " + ChatColor.DARK_PURPLE + killer.getName(),
                player.getLocation(),
                addon.getAddonManager().getPlugin().getCivConfig().getDeathMessageRange());

        event.setDeathMessage(null);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final PrisonPearl pearl = addon.getPrisonPearlManager().getPearl(player);

        if (pearl == null) {
            return;
        }

        final World end = Bukkit.getWorld("world_the_end");
        event.setRespawnLocation(end.getSpawnLocation());

        // You have been imprisoned by
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PrisonPearl pearl = addon.getPrisonPearlManager().getPearl(player);

        if (pearl == null || pearl.isExpired()) {
            return;
        }

        // TODO: Ensure player is in the end
    }


}