package com.playares.arena.kit;

import com.playares.arena.Arenas;
import com.playares.services.playerclasses.data.Class;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ClassKit extends Kit {
    @Getter public final Class attachedClass;

    public ClassKit(Arenas plugin, String name, ItemStack[] contents, ItemStack[] armor, Class playerClass) {
        super(plugin, name, contents, armor);
        this.attachedClass = playerClass;
    }

    @Override
    public void giveKit(Player player) {
        super.giveKit(player);
    }
}