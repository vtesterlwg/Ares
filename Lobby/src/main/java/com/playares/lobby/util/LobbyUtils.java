package com.playares.lobby.util;

import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.lobby.Lobby;
import com.playares.lobby.items.ServerSelectorItem;
import com.playares.services.customitems.CustomItemService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class LobbyUtils {
    public static void giveStandardItems(Lobby plugin, Player player) {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        if (customItemService != null) {
            customItemService.getItem(ServerSelectorItem.class).ifPresent(selector -> player.getInventory().setItem(4, selector.getItem()));
        }
    }

    public static void givePremiumItems(Player player) {
        final ItemStack elytra = new ItemBuilder()
                .setMaterial(Material.ELYTRA)
                .setName(ChatColor.GREEN + "Thank you for your support!")
                .addEnchant(Enchantment.DURABILITY, 5)
                .build();

        final ItemStack firework = new ItemBuilder()
                .setMaterial(Material.FIREWORK)
                .setName(ChatColor.GREEN + "Thank you for your support!")
                .setAmount(64)
                .build();

        player.getInventory().setChestplate(elytra);
        player.getInventory().setItemInOffHand(firework);
    }
}
