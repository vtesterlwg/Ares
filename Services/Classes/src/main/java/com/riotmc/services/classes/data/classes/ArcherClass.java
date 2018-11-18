package com.riotmc.services.classes.data.classes;

import com.google.common.collect.Lists;
import com.riotmc.services.classes.data.effects.ClassEffectable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public final class ArcherClass implements AresClass {
    @Override
    public String getName() {
        return "Archer";
    }

    @Override
    public List<String> getIntro() {
        final List<String> intro = Lists.newArrayList();

        intro.add(ChatColor.YELLOW + "Archer has increased bow damage against players at further distances.");
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
                return Material.SUGAR;
            }

            @Override
            public PotionEffect getEffect() {
                return new PotionEffect(PotionEffectType.SPEED, 5 * 20, 2);
            }

            @Override
            public int getCooldown() {
                return 60;
            }
        });

        effects.add(new ClassEffectable() {
            @Override
            public Material getMaterial() {
                return Material.RABBIT_FOOT;
            }

            @Override
            public PotionEffect getEffect() {
                return new PotionEffect(PotionEffectType.JUMP, 5 * 20, 4);
            }

            @Override
            public int getCooldown() {
                return 60;
            }
        });

        effects.add(new ClassEffectable() {
            @Override
            public Material getMaterial() {
                return Material.FEATHER;
            }

            @Override
            public PotionEffect getEffect() {
                return new PotionEffect(PotionEffectType.LEVITATION, 8 * 20, 0);
            }

            @Override
            public int getCooldown() {
                return 120;
            }
        });

        effects.add(new ClassEffectable() {
            @Override
            public Material getMaterial() {
                return Material.PHANTOM_MEMBRANE;
            }

            @Override
            public PotionEffect getEffect() {
                return new PotionEffect(PotionEffectType.SLOW_FALLING, 10 * 20, 0);
            }

            @Override
            public int getCooldown() {
                return 120;
            }
        });

        return effects;
    }

    @Override
    public List<PotionEffect> getPassiveEffects() {
        final List<PotionEffect> effects = Lists.newArrayList();

        effects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));

        return effects;
    }

    @Override
    public Material getHelmetType() {
        return Material.LEATHER_HELMET;
    }

    @Override
    public Material getChestplateType() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public Material getLeggingsType() {
        return Material.LEATHER_LEGGINGS;
    }

    @Override
    public Material getBootsType() {
        return Material.LEATHER_BOOTS;
    }
}
