package com.playares.commons.bukkit.util;

import org.bukkit.enchantments.Enchantment;

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
}