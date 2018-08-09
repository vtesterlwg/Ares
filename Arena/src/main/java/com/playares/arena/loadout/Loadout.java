package com.playares.arena.loadout;

import com.playares.commons.bukkit.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public interface Loadout {
    String getName();

    ItemStack[] getContents();

    ItemStack[] getArmor();

    default ItemStack getAsBook() {
        return new ItemBuilder()
                .setMaterial(Material.ENCHANTED_BOOK)
                .addFlag(ItemFlag.HIDE_ENCHANTS)
                .setName(ChatColor.AQUA + getName())
                .build();
    }

    default void apply(Player who) {
        who.getInventory().clear();
        who.getInventory().setArmorContents(null);

        who.getInventory().setContents(getContents());
        who.getInventory().setArmorContents(getArmor());
    }
}
