package com.playares.lobby.util;

import com.playares.commons.bukkit.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class LobbyUtils {
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
