package com.playares.services.playerclasses.data.cont;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.services.playerclasses.data.Class;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

public final class BardClass extends Class {
    @Getter @Setter public int warmup;
    @Getter @Setter public double range;

    public BardClass(int warmup, double range) {
        this.warmup = warmup;
        this.range = range;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
    }

    @Override
    public String getName() {
        return "Bard";
    }

    @Override
    public String getDescription() {
        return "Grants you the ability apply active effects to yourself and nearby players";
    }

    @Override
    public Material getRequiredHelmet() {
        return Material.GOLD_HELMET;
    }

    @Override
    public Material getRequiredChestplate() {
        return Material.GOLD_CHESTPLATE;
    }

    @Override
    public Material getRequiredLeggings() {
        return Material.GOLD_LEGGINGS;
    }

    @Override
    public Material getRequiredBoots() {
        return Material.GOLD_BOOTS;
    }
}