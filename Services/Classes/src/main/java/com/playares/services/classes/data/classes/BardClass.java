package com.playares.services.classes.data.classes;

import com.google.common.collect.Lists;
import com.playares.services.classes.data.effects.ClassEffectable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public final class BardClass implements AresClass {
    @Override
    public String getName() {
        return "Bard";
    }

    @Override
    public List<String> getIntro() {
        final List<String> intro = Lists.newArrayList();

        intro.add(ChatColor.YELLOW + "Bard can give potion effects to nearby friendly players.");
        intro.add(ChatColor.RESET + " ");
        intro.add(ChatColor.DARK_AQUA + "Consumables (effects apply to all nearby):");

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
                return 30;
            }
        });

        effects.add(new ClassEffectable() {
            @Override
            public Material getMaterial() {
                return Material.BLAZE_POWDER;
            }

            @Override
            public PotionEffect getEffect() {
                return new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3 * 20, 2);
            }

            @Override
            public int getCooldown() {
                return 60;
            }
        });

        effects.add(new ClassEffectable() {
            @Override
            public Material getMaterial() {
                return Material.IRON_INGOT;
            }

            @Override
            public PotionEffect getEffect() {
                return new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5 * 20, 1);
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
                return 30;
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
                return 300;
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
                return 180;
            }
        });

        effects.add(new ClassEffectable() {
            @Override
            public Material getMaterial() {
                return Material.GHAST_TEAR;
            }

            @Override
            public PotionEffect getEffect() {
                return new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 4);
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
        final List<PotionEffect> effect = Lists.newArrayList();

        effect.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

        return effect;
    }

    @Override
    public Material getHelmetType() {
        return Material.GOLDEN_HELMET;
    }

    @Override
    public Material getChestplateType() {
        return Material.GOLDEN_CHESTPLATE;
    }

    @Override
    public Material getLeggingsType() {
        return Material.GOLDEN_LEGGINGS;
    }

    @Override
    public Material getBootsType() {
        return Material.GOLDEN_BOOTS;
    }
}
