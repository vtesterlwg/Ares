package com.riotmc.services.essentials.data.kit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class Kit {
    @Getter
    public final String name;

    @Getter
    public final ItemStack[] contents;

    @Getter
    public final ItemStack[] armor;

    public void apply(Player player) {
        player.getInventory().clear();

        for (int i = 0; i < contents.length; i++) {
            final ItemStack item = contents[i];

            if (item == null) {
                continue;
            }

            player.getInventory().setItem(i, item);
        }

        for (int i = 0; i < armor.length; i++) {
            final ItemStack item = armor[i];

            if (item == null) {
                continue;
            }

            player.getInventory().setItem(i, item);
        }
    }
}