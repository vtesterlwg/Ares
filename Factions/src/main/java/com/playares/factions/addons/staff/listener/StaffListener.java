package com.playares.factions.addons.staff.listener;

import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.factions.addons.staff.StaffAddon;
import com.playares.factions.addons.staff.item.InvseeItem;
import com.playares.services.customitems.CustomItemService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor
public final class StaffListener implements Listener {
    @Getter public final StaffAddon addon;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (!player.hasPermission("factions.mod") && !player.hasPermission("factions.admin")) {
            return;
        }

        addon.giveItems(player);
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();

        if (!event.getType().equals(PlayerDamagePlayerEvent.DamageType.PHYSICAL)) {
            return;
        }

        if (attacker.getInventory().getItemInMainHand() == null || attacker.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            return;
        }

        final CustomItemService customItemService = (CustomItemService)addon.getPlugin().getService(CustomItemService.class);

        if (!attacker.hasPermission("factions.mod") && !attacker.hasPermission("factions.admin")) {
            return;
        }

        customItemService.getItem(attacker.getInventory().getItemInMainHand()).ifPresent(item -> {
            if (item instanceof InvseeItem) {
                attacker.performCommand("invsee " + attacked.getName());
                event.setCancelled(true);
            }
        });
    }
}
