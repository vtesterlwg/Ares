package com.playares.civilization.addons.prisonpearls;

import com.playares.civilization.addons.prisonpearls.data.PrisonPearl;
import com.playares.civilization.addons.prisonpearls.event.PlayerPearlEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PrisonPearlHandler {
    @Getter public final PrisonPearlManager manager;

    public PrisonPearlHandler(PrisonPearlManager manager) {
        this.manager = manager;
    }

    public PrisonPearl generatePrisonPearl(Player killer, Player killed, String reason) {
        final ItemStack pearlItem = getValidPearls(killer);

        if (pearlItem == null) {
            return null;
        }

        if (pearlItem.getAmount() <= 1) {
            pearlItem.setType(Material.AIR);
        } else {
            pearlItem.setAmount(pearlItem.getAmount() - 1);
        }

        // TODO: Duration from config

        final PlayerPearlEvent event = new PlayerPearlEvent(killer, killed, reason, 300);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        final PrisonPearl pearl = new PrisonPearl(killed, reason, event.getDuration());

        return pearl;
    }

    private ItemStack getValidPearls(Player player) {
        for (int i = 0; i < 8; i++) {
            final ItemStack item = player.getInventory().getItem(i);

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                continue;
            }

            if (manager.getPearl(item) != null) {
                continue;
            }

            return item;
        }

        return null;
    }
}
