package com.playares.services.playerclasses.data;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class Class {
    @Getter public Set<UUID> activePlayers;
    @Getter public Map<PotionEffectType, Integer> passiveEffects;
    @Getter public List<ClassConsumable> consumables;

    public abstract String getName();

    public abstract String getDescription();

    public abstract int getWarmup();

    public abstract Material getRequiredHelmet();

    public abstract Material getRequiredChestplate();

    public abstract Material getRequiredLeggings();

    public abstract Material getRequiredBoots();

    public void activate(Player player) {
        player.sendMessage(ChatColor.GOLD + "Class Activated" + ChatColor.YELLOW + ": " + ChatColor.BLUE + getName());
        player.sendMessage(ChatColor.GRAY + getDescription());

        passiveEffects.keySet().forEach(effectType -> {
            final int amplifier = passiveEffects.get(effectType);

            if (player.hasPotionEffect(effectType)) {
                player.removePotionEffect(effectType);
            }

            player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, amplifier));
        });

        activePlayers.add(player.getUniqueId());
    }

    public void deactivate(Player player) {
        player.sendMessage(ChatColor.GOLD + "Class Deactivated" + ChatColor.YELLOW + ": " + ChatColor.RED + getName());

        passiveEffects.keySet().forEach(player::removePotionEffect);

        activePlayers.remove(player.getUniqueId());
    }

    public void deactivate(Player player, boolean removeEffects) {
        player.sendMessage(ChatColor.GOLD + "Class Deactivated" + ChatColor.YELLOW + ": " + ChatColor.RED + getName());
        activePlayers.remove(player.getUniqueId());

        if (removeEffects) {
            passiveEffects.keySet().forEach(player::removePotionEffect);
        }
    }

    public ClassConsumable getConsumableByMaterial(Material material) {
        return consumables.stream().filter(consumable -> consumable.getMaterial().equals(material)).findFirst().orElse(null);
    }

    public boolean hasArmorRequirements(Player player) {
        // HELMET
        if (getRequiredHelmet() != null) {
            if (player.getInventory().getHelmet() == null) {
                return false;
            }

            if (!getRequiredHelmet().equals(player.getInventory().getHelmet().getType())) {
                return false;
            }
        }

        // CHESTPLATE
        if (getRequiredChestplate() != null) {
            if (player.getInventory().getChestplate() == null) {
                return false;
            }

            if (!getRequiredChestplate().equals(player.getInventory().getChestplate().getType())) {
                return false;
            }
        }

        // LEGGINGS
        if (getRequiredLeggings() != null) {
            if (player.getInventory().getLeggings() == null) {
                return false;
            }

            if (!getRequiredLeggings().equals(player.getInventory().getLeggings().getType())) {
                return false;
            }
        }

        // BOOTS
        if (getRequiredBoots() != null) {
            if (player.getInventory().getBoots() == null) {
                return false;
            }

            if (!getRequiredBoots().equals(player.getInventory().getBoots().getType())) {
                return false;
            }
        }

        return true;
    }
}
