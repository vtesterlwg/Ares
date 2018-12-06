package com.riotmc.factions.addons.events.data.loot;

import com.google.common.collect.ImmutableList;
import com.riotmc.commons.bukkit.location.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class PalaceChest extends BLocatable {
    @Getter @Setter public LootTable lootTable;

    public PalaceChest(@Nonnull String worldName, double x, double y, double z, LootTable lootTable) {
        super(worldName, x, y, z);
        this.lootTable = lootTable;
    }

    public void restock(Player viewer) {
        final Block block = getBukkit();

        if (block == null) {
            return;
        }

        final BlockState blockstate = block.getState();

        if (!(blockstate instanceof InventoryHolder)) {
            return;
        }

        final InventoryHolder inventory = (InventoryHolder)blockstate;

        inventory.getInventory().clear();

        final ImmutableList<ItemStack> loot = getLootTable().getLoot(viewer, 5);

        loot.forEach(i -> inventory.getInventory().addItem(i));
    }

    public enum LootStage {
        LOCKED, MONDAY, WEDNESDAY, FRIDAY, UNLOCKED
    }
}