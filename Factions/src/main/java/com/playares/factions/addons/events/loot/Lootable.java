package com.playares.factions.addons.events.loot;

import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.util.ItemUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

@AllArgsConstructor
public final class Lootable {
    @Getter public final EventsAddon addon;
    @Getter public final String materialName;
    @Getter public final String displayName;
    @Getter public final short data;
    @Getter public final int amount;
    @Getter public final Map<Enchantment, Integer> enchantments;
    @Getter public final int requiredPull;
    @Getter public final int totalPulls;

    public boolean pull() {
        return new Random().nextInt(totalPulls) <= requiredPull;
    }

    public ItemStack getItem() {
        final ItemStack byMaterial = ItemUtils.getItemByName(materialName);
        final ItemStack byId = ItemUtils.getItemById(materialName);
        final CustomItem customItem = ItemUtils.getCustomItem(addon.getPlugin(), materialName);

        if (customItem != null) {
            final ItemStack item = customItem.getItem();
            item.setAmount(amount);
            return item;
        }

        final ItemBuilder builder = new ItemBuilder();

        if (byMaterial != null) {
            builder.setMaterial(byMaterial.getType());

            if (displayName != null) {
                builder.setName(displayName);
            }

            if (data != 0) {
                builder.setData(data);
            }

            if (amount > 1) {
                builder.setAmount(amount);
            }

            if (!enchantments.isEmpty()) {
                builder.addEnchant(enchantments);
            }

            return builder.build();
        }

        if (byId != null) {
            builder.setMaterial(byId.getType());

            if (displayName != null) {
                builder.setName(displayName);
            }

            if (data != 0) {
                builder.setData(data);
            }

            if (amount > 1) {
                builder.setAmount(amount);
            }

            if (!enchantments.isEmpty()) {
                builder.addEnchant(enchantments);
            }

            return builder.build();
        }

        return null;
    }
}