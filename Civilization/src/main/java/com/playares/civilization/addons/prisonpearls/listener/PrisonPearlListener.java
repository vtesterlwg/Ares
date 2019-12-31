package com.playares.civilization.addons.prisonpearls.listener;

import com.playares.civilization.addons.prisonpearls.PrisonPearlAddon;
import com.playares.civilization.addons.prisonpearls.data.PrisonPearl;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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

        // TODO: Send slain player to the end
    }
}