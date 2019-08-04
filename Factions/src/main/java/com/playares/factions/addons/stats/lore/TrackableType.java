package com.playares.factions.addons.stats.lore;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public enum TrackableType {
    SWORD(ImmutableList.of(Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD)),
    PICKAXE(ImmutableList.of(Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE));

    @Getter
    public ImmutableList<Material> materials;

    public static TrackableType getTypeByItem(ItemStack item) {
        final Material material = item.getType();

        for (TrackableType val : values()) {
            if (val.getMaterials().contains(material)) {
                return val;
            }
        }

        return null;
    }
}
