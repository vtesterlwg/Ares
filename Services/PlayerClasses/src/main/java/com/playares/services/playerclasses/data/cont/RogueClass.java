package com.playares.services.playerclasses.data.cont;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.services.playerclasses.data.Class;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

public final class RogueClass extends Class {
    @Getter @Setter public int warmup;

    public RogueClass(int warmup) {
        this.warmup = warmup;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
    }

    @Override
    public String getName() {
        return "Rogue";
    }

    @Override
    public String getDescription() {
        return "Grants you the highest mobility of any class. Great for catching Archers and Bards or traveling the map quickly.";
    }

    @Override
    public Material getRequiredHelmet() {
        return Material.CHAINMAIL_HELMET;
    }

    @Override
    public Material getRequiredChestplate() {
        return Material.CHAINMAIL_CHESTPLATE;
    }

    @Override
    public Material getRequiredLeggings() {
        return Material.CHAINMAIL_LEGGINGS;
    }

    @Override
    public Material getRequiredBoots() {
        return Material.CHAINMAIL_BOOTS;
    }
}