package com.playares.arena.loadout.cont;

import com.playares.arena.loadout.Loadout;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public final class StandardLoadout implements Loadout {
    @Getter
    public final String name;

    @Getter
    public final ItemStack[] contents;

    @Getter
    public final ItemStack[] armor;

    public StandardLoadout(String name, ItemStack[] contents, ItemStack[] armor) {
        this.name = name;
        this.contents = contents;
        this.armor = armor;
    }
}
