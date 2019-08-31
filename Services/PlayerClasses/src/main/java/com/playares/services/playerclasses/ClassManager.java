package com.playares.services.playerclasses;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.playerclasses.data.Class;
import com.playares.services.playerclasses.data.ClassConsumable;
import com.playares.services.playerclasses.data.cont.ArcherClass;
import com.playares.services.playerclasses.data.cont.BardClass;
import com.playares.services.playerclasses.data.cont.MinerClass;
import com.playares.services.playerclasses.data.cont.RogueClass;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class ClassManager {
    @Getter public final PlayerClassService service;
    @Getter public final List<Class> classes;
    @Getter @Setter public BukkitTask passiveUpdater;

    ClassManager(PlayerClassService service) {
        this.service = service;
        this.classes = Lists.newArrayList();
    }

    void load() {
        final YamlConfiguration config = service.getOwner().getConfig("classes");

        if (config == null) {
            Logger.error("Failed to obtain classes.yml");
            return;
        }

        if (!classes.isEmpty()) {
            classes.clear();
            Logger.warn("Cleared classes while reloading " + getService().getName());
        }

        for (String className : config.getConfigurationSection("classes").getKeys(false)) {
            final int warmup = config.getInt("classes." + className + ".warmup");
            Class playerClass;

            if (className.equalsIgnoreCase("archer")) {
                final double maxDealtDamage = config.getDouble("classes." + className + ".damage-values.max-damage");
                final double damagePerBlock = config.getDouble("classes." + className + ".damage-values.increment");

                playerClass = new ArcherClass(warmup, maxDealtDamage, damagePerBlock);

                Logger.print("Archer values (Max Damage: " + maxDealtDamage + ", Damage Per Block: " + damagePerBlock + ")");
            } else if (className.equalsIgnoreCase("bard")) {
                final double range = config.getDouble("classes." + className + ".range");

                playerClass = new BardClass(warmup, range);

                Logger.print("Bard values (Range: " + range + ")");
            } else if (className.equalsIgnoreCase("rogue")) {
                playerClass = new RogueClass(warmup);
            } else if (className.equalsIgnoreCase("miner")) {
                playerClass = new MinerClass(warmup);
            } else {
                Logger.error("Invalid class name '" + className + "'");
                return;
            }

            if (config.get("classes." + className + ".passive") != null) {
                for (String passiveName : config.getConfigurationSection("classes." + className + ".passive").getKeys(false)) {
                    final PotionEffectType passiveType = PotionEffectType.getByName(passiveName);
                    final int amplifier = config.getInt("classes." + className + ".passive." + passiveName);

                    if (passiveType == null) {
                        Logger.error("Invalid passive effect type '" + passiveName + "'");
                        continue;
                    }

                    playerClass.getPassiveEffects().put(passiveType, (amplifier - 1));
                }
            }

            Logger.print("Loaded " + playerClass.getPassiveEffects().size() + " passive effects for " + className);

            if (config.get("classes." + className + ".consumables") != null) {
                for (String activeName : config.getConfigurationSection("classes." + className + ".consumables").getKeys(false)) {
                    final String path = "classes." + className + ".consumables." + activeName + ".";
                    final String materialName = config.getString(path + "material");
                    final int duration = config.getInt(path + "duration");
                    final int amplifier = config.getInt(path + "amplifier");
                    final int cooldown = config.getInt(path + "cooldown");
                    final PotionEffectType effect = PotionEffectType.getByName(activeName);
                    final Material material;
                    final ClassConsumable.ConsumableApplicationType applicationType;

                    try {
                        material = Material.valueOf(materialName);
                    } catch (IllegalArgumentException ex) {
                        Logger.error("Illegal Material", ex);
                        continue;
                    }

                    try {
                        applicationType = ClassConsumable.ConsumableApplicationType.valueOf(config.getString(path + "application"));
                    } catch (IllegalArgumentException ex) {
                        Logger.error("Illegal ConsumableApplicationType", ex);
                        continue;
                    }

                    final ClassConsumable consumable = new ClassConsumable(service,
                            material,
                            duration,
                            cooldown,
                            applicationType,
                            effect,
                            amplifier);

                    playerClass.getConsumables().add(consumable);
                }
            }

            Logger.print("Loaded " + playerClass.getConsumables().size() + " Consumables for " + className);

            classes.add(playerClass);
        }
    }

    public Class getCurrentClass(Player player) {
        return classes.stream().filter(playerClass -> playerClass.getActivePlayers().contains(player.getUniqueId())).findFirst().orElse(null);
    }

    public Class getClassByArmor(Player player) {
        return classes.stream().filter(playerClass -> playerClass.hasArmorRequirements(player)).findFirst().orElse(null);
    }
}