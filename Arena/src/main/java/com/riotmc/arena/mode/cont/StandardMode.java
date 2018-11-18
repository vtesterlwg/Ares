package com.riotmc.arena.mode.cont;

import com.google.common.collect.Lists;
import com.riotmc.arena.loadout.Loadout;
import com.riotmc.arena.mode.Mode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class StandardMode implements Mode {
    @Nonnull @Getter
    public final String name;

    @Nullable @Getter @Setter
    public ItemStack icon;

    @Nonnull @Getter
    public List<Loadout> loadouts;

    public StandardMode(@Nonnull String name) {
        this.name = name;
        this.icon = null;
        this.loadouts = Lists.newArrayList();
    }
}