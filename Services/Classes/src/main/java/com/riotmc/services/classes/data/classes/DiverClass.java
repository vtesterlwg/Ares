package com.riotmc.services.classes.data.classes;

import com.google.common.collect.Lists;
import com.riotmc.services.classes.data.effects.ClassEffectable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public final class DiverClass implements AresClass {
    @Override
    public String getName() {
        return "Diver";
    }

    @Override
    public List<String> getIntro() {
        final List<String> intro = Lists.newArrayList();

        intro.add(ChatColor.YELLOW + "Diver has improved underwater visibility, mining speed and can not drown.");
        intro.add(ChatColor.RESET + " ");
        intro.add(ChatColor.DARK_AQUA + "Consumables:");

        for (ClassEffectable effect : getConsumeables()) {
            intro.add(ChatColor.BLUE + StringUtils.capitaliseAllWords(effect.getMaterial().getKey().getKey().toLowerCase().replace("_", " ")) + ChatColor.AQUA + ": " +
                    ChatColor.WHITE + StringUtils.capitaliseAllWords(effect.getEffect().getType().getName().toLowerCase().replace("_", " ") +
                    " " + (effect.getEffect().getAmplifier() + 1) + " for " + (effect.getEffect().getDuration() / 20)) + " seconds");
        }

        return intro;
    }

    @Override
    public List<ClassEffectable> getConsumeables() {
        final List<ClassEffectable> effects = Lists.newArrayList();

        effects.add(new ClassEffectable() {
            @Override
            public Material getMaterial() {
                return Material.NAUTILUS_SHELL;
            }

            @Override
            public PotionEffect getEffect() {
                return new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 3 * 20, 0);
            }

            @Override
            public int getCooldown() {
                return 60;
            }
        });

        return effects;
    }

    @Override
    public List<PotionEffect> getPassiveEffects() {
        final List<PotionEffect> effects = Lists.newArrayList();

        effects.add(new PotionEffect(PotionEffectType.CONDUIT_POWER, Integer.MAX_VALUE, 0));

        return effects;
    }

    @Override
    public Material getHelmetType() {
        return Material.TURTLE_HELMET;
    }

    @Override
    public Material getChestplateType() {
        return Material.CHAINMAIL_CHESTPLATE;
    }

    @Override
    public Material getLeggingsType() {
        return Material.CHAINMAIL_LEGGINGS;
    }

    @Override
    public Material getBootsType() {
        return Material.CHAINMAIL_BOOTS;
    }
}
