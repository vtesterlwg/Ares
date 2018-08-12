package com.playares.arena.loadout.cont;

import com.playares.arena.Arenas;
import com.playares.arena.loadout.Loadout;
import com.playares.services.classes.ClassService;
import com.playares.services.classes.data.classes.AresClass;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ClassLoadout implements Loadout {
    @Getter
    public final Arenas plugin;

    @Getter
    public final String name;

    @Getter
    public final ItemStack[] contents;

    @Getter
    public final ItemStack[] armor;

    @Getter
    public final AresClass classType;

    public ClassLoadout(Arenas plugin, String name, ItemStack[] contents, ItemStack[] armor, AresClass type) {
        this.plugin = plugin;
        this.name = name;
        this.contents = contents;
        this.armor = armor;
        this.classType = type;
    }

    @Override
    public void apply(Player who) {
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
