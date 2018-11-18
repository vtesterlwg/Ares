package com.riotmc.arena.loadout.cont;

import com.riotmc.arena.loadout.Loadout;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class StandardLoadout implements Loadout {
    @Nonnull @Getter
    public final String name;

    @Nonnull @Getter
    public final ItemStack[] contents;

    @Nonnull @Getter
    public final ItemStack[] armor;

    public StandardLoadout(@Nonnull String name, @Nonnull ItemStack[] contents, @Nonnull ItemStack[] armor) {
        this.name = name;
        this.contents = contents;
        this.armor = armor;
    }
}
