package com.riotmc.services.classes.data.classes;

import com.google.common.base.Joiner;
import com.riotmc.services.classes.data.effects.ClassEffectable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public interface AresClass {
    String getName();

    List<String> getIntro();

    List<ClassEffectable> getConsumeables();

    List<PotionEffect> getPassiveEffects();

    Material getHelmetType();

    Material getChestplateType();

    Material getLeggingsType();

    Material getBootsType();

    default ClassEffectable getConsumable(Material material) {
        return getConsumeables().stream().filter(c -> c.getMaterial().equals(material)).findFirst().orElse(null);
    }

    default boolean match(Player player) {
        final ItemStack helmet = player.getInventory().getHelmet();
        final ItemStack chestplate = player.getInventory().getChestplate();
        final ItemStack leggings = player.getInventory().getLeggings();
        final ItemStack boots = player.getInventory().getBoots();

        if (helmet == null || !helmet.getType().equals(getHelmetType())) {
            return false;
        }

        if (chestplate == null || !chestplate.getType().equals(getChestplateType())) {
            return false;
        }

        if (leggings == null || !leggings.getType().equals(getLeggingsType())) {
            return false;
        }

        if (boots == null || !boots.getType().equals(getBootsType())) {
            return false;
        }

        return true;
    }

    default void sendIntro(Player viewer) {
        viewer.sendMessage(Joiner.on(ChatColor.RESET + "\n").join(getIntro()));
    }
}