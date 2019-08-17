package com.playares.services.essentials.data.kit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class Kit {
    @Getter public final String name;
    @Getter public final ItemStack[] contents;
    @Getter public final ItemStack[] armor;

    public void apply(Player player) {
        player.getInventory().clear();
        player.getInventory().setStorageContents(contents);
        player.getInventory().setArmorContents(armor);
    }
}