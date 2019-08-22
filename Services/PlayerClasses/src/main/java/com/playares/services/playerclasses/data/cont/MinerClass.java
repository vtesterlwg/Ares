package com.playares.services.playerclasses.data.cont;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.services.playerclasses.data.Class;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

public final class MinerClass extends Class {
    @Getter @Setter public int warmup;

    public MinerClass(int warmup) {
        this.warmup = warmup;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
    }

    @Override
    public String getName() {
        return "Miner";
    }

    @Override
    public String getDescription() {
        return "Grants you passive effects that will help you on your mining trips";
    }

    @Override
    public Material getRequiredHelmet() {
        return Material.IRON_HELMET;
    }

    @Override
    public Material getRequiredChestplate() {
        return Material.IRON_CHESTPLATE;
    }

    @Override
    public Material getRequiredLeggings() {
        return Material.IRON_LEGGINGS;
    }

    @Override
    public Material getRequiredBoots() {
        return Material.IRON_BOOTS;
    }
}
