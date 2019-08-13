package com.playares.factions.util;

import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.factions.Factions;
import com.playares.services.customitems.CustomItemService;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public final class ItemUtils {
    /**
     * Returns an ItemStack which matches the provided name
     * @param name Item name
     * @return ItemStack
     */
    @Nullable
    public static ItemStack getItemByName(String name) {
        if (name.contains(":")) {
            final String materialName = name.split(":")[0].toUpperCase();
            final String idAsString = name.split(":")[1];
            final int id;

            try {
                id = Integer.parseInt(idAsString);
            } catch (NumberFormatException ex) {
                return null;
            }

            final Material material = Material.getMaterial(materialName);

            if (material != null) {
                return new ItemBuilder()
                        .setMaterial(material)
                        .setData((short)id)
                        .build();
            }

            return null;
        }

        final Material material = Material.getMaterial(name.toUpperCase());

        if (material == null) {
            return null;
        }

        return new ItemBuilder()
                .setMaterial(material)
                .build();
    }

    /**
     * Returns an ItemStack matching the provided item id
     * @param value Item:ItemID
     * @return ItemStack
     */
    @Nullable
    public static ItemStack getItemById(String value) {
        if (value.contains(":")) {
            final String itemIdAsString = value.split(":")[0];
            final String itemDataAsString = value.split(":")[1];
            final int id;
            final int data;

            try {
                id = Integer.parseInt(itemIdAsString);
                data = Integer.parseInt(itemDataAsString);
            } catch (NumberFormatException ex) {
                return null;
            }

            final Material material = Material.getMaterial(id);

            if (material != null) {
                return new ItemBuilder()
                        .setMaterial(material)
                        .setData((short)data)
                        .build();
            }

            return null;
        }

        final int id;

        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }

        final Material material = Material.getMaterial(id);

        if (material == null) {
            return null;
        }

        return new ItemBuilder()
                .setMaterial(material)
                .build();
    }

    /**
     * Returns a CustomItem matching the provided name
     * @param plugin Factions plugin
     * @param name Item Name
     * @return CustomItem
     */
    @Nullable
    public static CustomItem getCustomItem(Factions plugin, String name) {
        final CustomItemService service = (CustomItemService)plugin.getService(CustomItemService.class);

        if (service == null) {
            return null;
        }

        final Optional<CustomItem> item = service.getItem(name);

        return item.orElse(null);
    }
}
