package com.playares.factions.addons.events.loot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.PalaceEvent;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class LootManager {
    @Getter public final EventsAddon addon;
    @Getter public final Set<Lootable> standardLoot;
    @Getter public final Set<Lootable> palaceLoot;

    public LootManager(EventsAddon addon) {
        this.addon = addon;
        this.standardLoot = Sets.newHashSet();
        this.palaceLoot = Sets.newHashSet();
    }

    public void load() {
        final YamlConfiguration config = getAddon().getPlugin().getConfig("events");

        for (String materialName : config.getConfigurationSection("standard-loot-table").getKeys(false)) {
            String name = null;
            short data = 0;
            int amount = 1;
            Map<Enchantment, Integer> enchantments = Maps.newHashMap();
            int required = 1;
            int total = 100;

            if (config.get("standard-loot-table." + materialName + ".name") != null) {
                name = ChatColor.translateAlternateColorCodes('&', config.getString("standard-loot-table." + materialName + ".name"));
            }

            if (config.get("standard-loot-table." + materialName + ".data") != null) {
                data = (short)config.getInt("standard-loot-table." + materialName + ".data");
            }

            if (config.get("standard-loot-table." + materialName + ".amount") != null) {
                amount = config.getInt("standard-loot-table." + materialName + ".amount");
            }

            if (config.get("standard-loot-table." + materialName + ".enchantments") != null) {
                final List<String> values = config.getStringList("standard-loot-table." + materialName + ".enchantments");

                for (String value : values) {
                    final String enchantmentName = value.split(":")[0];
                    final String levelName = value.split(":")[1];
                    final Enchantment enchantment = Enchantment.getByName(enchantmentName);
                    final int level;

                    if (enchantment == null) {
                        Logger.error("Failed to load enchantment for loot table item, unknown enchantment '" + enchantmentName + "'");
                        continue;
                    }

                    try {
                        level = Integer.parseInt(levelName);
                    } catch (NumberFormatException ex) {
                        Logger.error("Failed to load enchantment for loot table item, level must be a number, found '" + levelName + "' instead");
                        continue;
                    }

                    enchantments.put(enchantment, level);
                }
            }

            if (config.get("standard-loot-table." + materialName + ".chance") != null) {
                final String requiredName = config.getString("standard-loot-table." + materialName + ".chance").split(":")[0];
                final String totalName = config.getString("standard-loot-table." + materialName + ".chance").split(":")[1];

                try {
                    required = Integer.parseInt(requiredName);
                    total = Integer.parseInt(totalName);
                } catch (NumberFormatException ex) {
                    Logger.error("Failed to load chance for loot table item");
                    continue;
                }
            }

            final Lootable loot = new Lootable(getAddon(), materialName, name, data, amount, enchantments, required, total);
            standardLoot.add(loot);
            Logger.print("Loaded " + standardLoot.size() + " lootable items in to the " + LootType.STANDARD.name() + " loot table");
        }
    }

    public List<ItemStack> getLoot(LootType type, int amount) {
        final List<ItemStack> loot = Lists.newArrayList();
        final Set<Lootable> toSearch = (type.equals(LootType.STANDARD)) ? standardLoot : palaceLoot;
        int currentAttempt = 0;
        final int maxAttempts = 1000;

        while (loot.size() < amount && currentAttempt < maxAttempts) {
            for (Lootable item : toSearch) {
                if (item.pull()) {
                    loot.add(item.getItem());
                }
            }

            currentAttempt += 1;
        }

        return loot;
    }

    public void fillCaptureChest(AresEvent event) {
        final Block lootChest = event.getCaptureChestLocation().getBukkit();

        if (lootChest == null) {
            Logger.error("Loot chest block was null for event " + event.getName());
            return;
        }

        if (!lootChest.getType().equals(Material.CHEST)) {
            Logger.error("Loot chest block was not found as a chest for event " + event.getName());
            return;
        }

        final Chest chest = (Chest)lootChest.getState();
        final List<ItemStack> loot = getAddon().getLootManager().getLoot(LootType.STANDARD, (event instanceof PalaceEvent) ? 10 : 5);
        final Set<Integer> slots = Sets.newHashSet();
        int pos = 0;

        while (slots.size() < loot.size()) {
            slots.add(Math.abs(new Random().nextInt(26)));
        }

        for (int slot : slots) {
            final ItemStack item = loot.get(pos);
            chest.getBlockInventory().setItem(slot, item);
            pos += 1;
        }
    }
}