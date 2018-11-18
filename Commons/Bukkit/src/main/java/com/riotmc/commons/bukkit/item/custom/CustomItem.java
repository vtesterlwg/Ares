package com.riotmc.commons.bukkit.item.custom;

import com.riotmc.commons.bukkit.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface CustomItem {
    /**
     * @return Bukkit Material
     */
    Material getMaterial();

    /**
     * @return Item Display Name
     */
    String getName();

    /**
     * @return Item Lore
     */
    List<String> getLore();

    /**
     * @return Item Enchantments
     */
    Map<Enchantment, Integer> getEnchantments();

    /**
     * Left click action
     * @param who Clicking player
     * @return Runnable task
     */
    default Runnable getLeftClick(Player who) {
        return null;
    }

    /**
     * Right click action
     * @param who Clicking player
     * @return Runnable task
     */
    default Runnable getRightClick(Player who) {
        return null;
    }

    /**
     * @return True if this item can be repaired in an anvil
     */
    default boolean isRepairable() {
        return false;
    }

    /**
     * @return True if this item can't be dropped
     */
    default boolean isSoulbound() {
        return true;
    }

    /**
     * Returns this item in Bukkit form
     * @return ItemStack
     */
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