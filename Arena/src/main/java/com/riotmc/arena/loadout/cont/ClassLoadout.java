package com.riotmc.arena.loadout.cont;

import com.riotmc.arena.Arenas;
import com.riotmc.arena.loadout.Loadout;
import com.riotmc.services.classes.ClassService;
import com.riotmc.services.classes.data.classes.AresClass;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class ClassLoadout implements Loadout {
    @Nonnull @Getter
    public final Arenas plugin;

    @Nonnull @Getter
    public final String name;

    @Nonnull @Getter
    public final ItemStack[] contents;

    @Nonnull @Getter
    public final ItemStack[] armor;

    @Nonnull @Getter
    public final AresClass classType;

    public ClassLoadout(@Nonnull Arenas plugin,
                        @Nonnull String name,
                        @Nonnull ItemStack[] contents,
                        @Nonnull ItemStack[] armor,
                        @Nonnull AresClass type) {

        this.plugin = plugin;
        this.name = name;
        this.contents = contents;
        this.armor = armor;
        this.classType = type;

    }

    @Override
    public void apply(@Nonnull Player who) {
        who.getInventory().clear();
        who.getInventory().setArmorContents(null);

        who.getInventory().setContents(getContents());
        who.getInventory().setArmorContents(getArmor());

        final ClassService classService = (ClassService)plugin.getService(ClassService.class);

        if (classService == null) {
            return;
        }

        if (classService.getClassByArmor(who).equals(classType)) {
            classService.addToClass(who, classType);
        }
    }
}