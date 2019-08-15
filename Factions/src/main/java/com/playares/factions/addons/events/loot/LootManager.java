package com.playares.factions.addons.events.loot;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.addons.events.data.type.koth.PalaceEvent;
import com.playares.factions.addons.events.loot.palace.PalaceLootTier;
import com.playares.factions.addons.events.loot.palace.PalaceLootable;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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
import java.util.stream.Collectors;

public final class LootManager {
    @Getter public final EventsAddon addon;
    private final Set<Lootable> standardLoot;
    private final Set<PalaceLootable> palaceLoot;

    public LootManager(EventsAddon addon) {
        this.addon = addon;
        this.standardLoot = Sets.newHashSet();
        this.palaceLoot = Sets.newHashSet();
    }

    public void load() {
        final YamlConfiguration config = getAddon().getPlugin().getConfig("events");

        for (String materialName : config.getConfigurationSection("standard-loot-table").getKeys(false)) {
            final String path = "standard-loot-table." + materialName + ".";
            String name = null;
            short data = 0;
            int amount = 1;
            final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
            int required = 1;
            int total = 100;

            if (config.get(path + "name") != null) {
                name = ChatColor.translateAlternateColorCodes('&', config.getString(path + "name"));
            }

            if (config.get(path + "data") != null) {
                data = (short)config.getInt(path + "data");
            }

            if (config.get(path + "amount") != null) {
                amount = config.getInt(path + "amount");
            }

            if (config.get(path + "enchantments") != null) {
                final List<String> values = config.getStringList(path + "enchantments");

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

            if (config.get(path + "chance") != null) {
                final String requiredName = config.getString(path + "chance").split(":")[0];
                final String totalName = config.getString(path + "chance").split(":")[1];

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
        }

        Logger.print("Loaded " + standardLoot.size() + " lootable items in to the " + LootType.STANDARD.name() + " loot table");

        for (PalaceLootTier tier : PalaceLootTier.values()) {
            for (String materialName : config.getConfigurationSection("palace-loot-table." + tier.name()).getKeys(false)) {
                final String path = "palace.loot-table." + tier.name() + "." + materialName + ".";
                String name = null;
                short data = 0;
                int amount = 1;
                final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
                int required = 1;
                int total = 100;

                if (config.get(path + "name") != null) {
                    name = ChatColor.translateAlternateColorCodes('&', config.getString(path + "name"));
                }

                if (config.get(path + "data") != null) {
                    data = (short)config.getInt(path + "data");
                }

                if (config.get(path + "amount") != null) {
                    amount = config.getInt(path + "amount");
                }

                if (config.get(path + "enchantments") != null) {
                    final List<String> values = config.getStringList(path + "enchantments");

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

                if (config.get(path + "chance") != null) {
                    final String requiredName = config.getString(path + "chance").split(":")[0];
                    final String totalName = config.getString(path + "chance").split(":")[1];

                    try {
                        required = Integer.parseInt(requiredName);
                        total = Integer.parseInt(totalName);
                    } catch (NumberFormatException ex) {
                        Logger.error("Failed to load chance for loot table item");
                        continue;
                    }
                }

                final PalaceLootable loot = new PalaceLootable(getAddon(), tier, materialName, name, data, amount, enchantments, required, total);
                palaceLoot.add(loot);
            }

            Logger.print("Loaded " + palaceLoot.stream().filter(palaceLoot -> palaceLoot.getTier().equals(tier)).count() + " lootable items in to the " + LootType.PALACE.name() + " " + tier.name() + " loot table");
        }
    }

    public List<ItemStack> getStandardLoot(int amount) {
        final List<ItemStack> loot = Lists.newArrayList();
        int currentAttempt = 0;
        final int maxAttempts = 1000;

        while (loot.size() < amount && currentAttempt < maxAttempts) {
            for (Lootable item : standardLoot) {
                if (item.pull()) {
                    loot.add(item.getItem());
                }
            }

            currentAttempt += 1;
        }

        return loot;
    }

    public List<ItemStack> getPalaceLoot(PalaceLootTier tier, int amount) {
        final List<ItemStack> loot = Lists.newArrayList();
        int currentAttempt = 0;
        final int maxAttempts = 1000;

        while (loot.size() < amount && currentAttempt < maxAttempts) {
            for (PalaceLootable item : palaceLoot.stream().filter(palaceLoot -> palaceLoot.getTier().equals(tier)).collect(Collectors.toList())) {
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
        final List<ItemStack> loot = getAddon().getLootManager().getStandardLoot((event instanceof PalaceEvent) ? 10 : 5);
        final Set<Integer> slots = Sets.newHashSet();
        int pos = 0;

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (event instanceof KOTHEvent) {
                final KOTHEvent koth = (KOTHEvent)event;
                final List<String> lootNames = Lists.newArrayList();

                for (ItemStack item : loot) {
                    if (item == null) {
                        continue;
                    }

                    lootNames.add(ChatColor.YELLOW + "" + item.getAmount() + "x " +
                            ((item.hasItemMeta() && item.getItemMeta().getDisplayName() != null) ? item.getItemMeta().getDisplayName() : StringUtils.capitaliseAllWords(item.getType().name().toLowerCase().replace("_", " "))));
                }

                player.sendMessage(EventsAddon.PREFIX + ChatColor.BLUE + koth.getSession().getCapturingFaction().getName() + ChatColor.GOLD + " received " + Joiner.on(ChatColor.GOLD + ", ").join(lootNames) + ChatColor.GOLD + " from " + ChatColor.RESET + event.getDisplayName());
            }
        });

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