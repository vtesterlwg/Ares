package com.playares.arena.mode.cont;

import com.google.common.collect.Lists;
import com.playares.arena.loadout.Loadout;
import com.playares.arena.mode.Mode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class StandardMode implements Mode {
    @Getter
    public final String name;

    @Getter @Setter
    public ItemStack icon;

    @Getter
    public List<Loadout> loadouts;

    public StandardMode(String name) {
        this.name = name;
        this.icon = null;
        this.loadouts = Lists.newArrayList();
    }
}