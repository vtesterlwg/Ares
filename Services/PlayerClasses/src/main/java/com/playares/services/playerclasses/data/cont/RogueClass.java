package com.playares.services.playerclasses.data.cont;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.services.playerclasses.data.Class;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class RogueClass extends Class {
    @Getter @Setter public int warmup;
    @Getter @Setter public int backstabCooldown;
    @Getter @Setter public double backstabDamage;
    @Getter public final Map<UUID, Long> backstabCooldowns;

    public RogueClass(int warmup, int backstabCooldown, double backstabDamage) {
        this.warmup = warmup;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.backstabCooldown = backstabCooldown;
        this.backstabDamage = backstabDamage;
        this.backstabCooldowns = Maps.newConcurrentMap();
    }

    public boolean hasBackstabCooldown(Player player) {
        return backstabCooldowns.containsKey(player.getUniqueId());
    }

    @Override
    public String getName() {
        return "Rogue";
    }

    @Override
    public String getDescription() {
        return "Grants you the ability to backstab players with Gold Swords.";
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