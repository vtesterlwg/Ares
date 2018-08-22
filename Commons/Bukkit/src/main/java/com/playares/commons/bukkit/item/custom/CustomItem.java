package com.playares.commons.bukkit.item.custom;

import com.playares.commons.bukkit.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface CustomItem {
    Material getMaterial();

    String getName();

    List<String> getLore();

    Map<Enchantment, Integer> getEnchantments();

    default Runnable getLeftClick(Player who) {
        return null;
    }

    default Runnable getRightClick(Player who) {
        return null;
    }

    default boolean isRepairable() {
        return false;
    }

    default boolean isSoulbound() {
        return true;
    }

    default ItemStack getItem() {
        if (!isRepairable()) {
            getLore().add(ChatColor.RED + "Unrepairable");
        }

        if (isSoulbound()) {
            getLore().add(ChatColor.RED + "Soulbound");
        }

        return new ItemBuilder().setMaterial(getMaterial()).setName(getName()).addLore(getLore()).addEnchant(getEnchantments()).build();
    }
}
