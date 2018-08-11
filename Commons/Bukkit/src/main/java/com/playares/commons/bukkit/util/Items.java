package com.playares.commons.bukkit.util;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class Items {
    public static Enchantment getEnchantmentByName(String name) {
        for (Enchantment enchantment : Enchantment.values()) {
            final String snip = enchantment.getKey().getKey().substring(0, 4);

            if (name.startsWith(snip)) {
                return enchantment;
            }
        }

        return null;
    }

    public static boolean isArmor(ItemStack item) {
        Preconditions.checkNotNull(item, "Provided ItemStack was null");

        final String name = item.getType().toString();

        if (
                name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
                        name.equalsIgnoreCase("CARVED_PUMPKIN") || name.equalsIgnoreCase("ELYTRA")) {

            return true;

        }

        return false;
    }

    public static boolean isHelmet(ItemStack item) {
        Preconditions.checkNotNull(item, "Provided ItemStack was null");

        final String name = item.getType().toString();

        return name.endsWith("_HELMET") || name.endsWith("_SKULL") || name.equalsIgnoreCase("CARVED_PUMPKIN");
    }

    public static boolean isChestplate(ItemStack item) {
        Preconditions.checkNotNull(item, "Provided ItemStack was null");

        final String name = item.getType().toString();

        return name.endsWith("_CHESTPLATE") || name.equalsIgnoreCase("ELYTRA");
    }

    public static boolean isLeggings(ItemStack item) {
        Preconditions.checkNotNull(item, "Provided ItemStack was null");

        final String name = item.getType().toString();

        return name.endsWith("_LEGGINGS");
    }

    public static boolean isBoots(ItemStack item) {
        Preconditions.checkNotNull(item, "Provided ItemStack was null");

        final String name = item.getType().toString();

        return name.endsWith("_BOOTS");
    }

    public static boolean isInteractable(Material material) {
        return
                material.name().endsWith("_CHEST") || material.equals(Material.CRAFTING_TABLE) || material.equals(Material.FURNACE) ||
                material.equals(Material.ENCHANTING_TABLE) || material.name().endsWith("ANVIL") || material.equals(Material.ITEM_FRAME) ||
                material.name().endsWith("_BED") || material.equals(Material.LEVER) || material.name().endsWith("_PLATE") ||
                material.name().endsWith("_BUTTON") || material.name().endsWith("_TRAPDOOR") || material.name().endsWith("_FENCE_GATE") ||
                material.equals(Material.DAYLIGHT_DETECTOR) || material.equals(Material.HOPPER) || material.equals(Material.DROPPER) ||
                material.equals(Material.OBSERVER) || material.name().endsWith("_DOOR") || material.equals(Material.REPEATER) ||
                material.equals(Material.COMPARATOR) || material.equals(Material.BEACON) || material.equals(Material.CAULDRON) ||
                material.equals(Material.FLOWER_POT) || material.name().endsWith("SHULKER_BOX");
    }
}