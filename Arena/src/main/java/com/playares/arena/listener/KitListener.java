package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.kit.Kit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class KitListener implements Listener {
    @Getter public final Arenas plugin;

    @EventHandler
    public void onBookClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();
        final ItemStack item = event.getItem();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        if (item == null || !item.getType().equals(Material.ENCHANTED_BOOK)) {
            return;
        }

        if (!item.hasItemMeta() || item.getItemMeta().getDisplayName() == null || !item.getItemMeta().getDisplayName().startsWith(ChatColor.YELLOW + "Load Kit: " + ChatColor.AQUA)) {
            return;
        }

        final String kitName = item.getItemMeta().getDisplayName().replace(ChatColor.YELLOW + "Load Kit: " + ChatColor.AQUA, "");
        final Kit kit = plugin.getKitManager().getKit(kitName);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Invalid kit book");
            return;
        }

        kit.giveKit(player);
    }
}
