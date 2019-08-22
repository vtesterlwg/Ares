package com.playares.services.playerclasses.data.cont;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.services.playerclasses.data.Class;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

public final class ArcherClass extends Class {
    @Getter @Setter public int warmup;
    @Getter @Setter public double maxDealtDamage;
    @Getter @Setter public double damagePerBlock;

    public ArcherClass(int warmup, double maxDealtDamage, double damagePerBlock) {
        this.warmup = warmup;
        this.maxDealtDamage = maxDealtDamage;
        this.damagePerBlock = damagePerBlock;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
    }

    @Override
    public String getName() {
        return "Archer";
    }

    @Override
    public String getDescription() {
        return "Grants you bonus damage with a bow based on distance";
    }

    @Override
    public Material getRequiredHelmet() {
        return Material.LEATHER_HELMET;
    }

    @Override
    public Material getRequiredChestplate() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public Material getRequiredLeggings() {
        return Material.LEATHER_LEGGINGS;
    }

    @Override
    public Material getRequiredBoots() {
        return Material.LEATHER_BOOTS;
    }
}