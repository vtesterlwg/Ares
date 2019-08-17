package com.playares.factions.addons.events.loot;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.addons.events.data.type.koth.PalaceEvent;
import com.playares.factions.addons.events.loot.palace.PalaceLootChest;
import com.playares.factions.addons.events.loot.palace.PalaceLootTier;
import com.playares.factions.addons.events.loot.palace.PalaceLootable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public final class LootManager {
    @Getter public final EventsAddon addon;
    @Getter @Setter public BukkitTask palaceLootTimer;
    @Getter public final Set<Lootable> standardLootables;
    @Getter public final Set<PalaceLootable> palaceLootables;

    public LootManager(EventsAddon addon) {
        this.addon = addon;
        this.standardLootables = Sets.newHashSet();
        this.palaceLootables = Sets.newHashSet();
    }

    public void load() {
        loadStandardLoot();
        loadPalaceLoot();
        loadPalaceChests();

        palaceLootTimer = new Scheduler(getAddon().getPlugin()).async(() -> addon.getManager().getPalaceEvents().forEach(PalaceEvent::stock)).repeat(3600 * 20, 3600 * 20).run();
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

    public void fillPalaceChest(PalaceLootChest chest) {
        final Block block = chest.getBukkit();

        if (block == null || !block.getType().equals(Material.CHEST)) {
            Logger.warn("Palace chest at " + chest.toString() + " is not a chest and has been skipped");
            return;
        }

        final Chest chestBlock = (Chest)block.getState();
        final List<ItemStack> loot = getPalaceLoot(chest.getTier(), 3);
        final Set<Integer> slots = Sets.newHashSet();
        int pos = 0;

        while (slots.size() < loot.size()) {
            slots.add(Math.abs(new Random().nextInt(26)));
        }

        for (int slot : slots) {
            final ItemStack item = loot.get(pos);
            chestBlock.getBlockInventory().setItem(slot, item);
            pos += 1;
        }
    }

    private void loadStandardLoot() {
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
            standardLootables.add(loot);
        }

        Logger.print("Loaded " + standardLootables.size() + " lootable items in to the " + LootType.STANDARD.name() + " loot table");
    }

    private void loadPalaceLoot() {
        final YamlConfiguration config = getAddon().getPlugin().getConfig("events");

        for (PalaceLootTier tier : PalaceLootTier.values()) {
            for (String materialName : config.getConfigurationSection("palace-loot-table." + tier.name()).getKeys(false)) {
                final String path = "palace-loot-table." + tier.name() + "." + materialName + ".";
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
                palaceLootables.add(loot);
            }

            Logger.print("Loaded " + palaceLootables.stream().filter(palaceLoot -> palaceLoot.getTier().equals(tier)).count() + " lootable items in to the " + LootType.PALACE.name() + " " + tier.name() + " loot table");
        }
    }

    private void loadPalaceChests() {
        final YamlConfiguration config = getAddon().getPlugin().getConfig("events");

        if (config.get("palace-chests") == null) {
            Logger.warn("No palace chests were found in events.yml... Skipping loading Palace Chests");
            return;
        }

        for (String palaceEventName : config.getConfigurationSection("palace-chests").getKeys(false)) {
            final AresEvent event = getAddon().getManager().getEventByName(palaceEventName);

            if (!(event instanceof PalaceEvent)) {
                Logger.error("Failed to find Palace Event by name '" + palaceEventName + "', skipping...");
                continue;
            }

            final PalaceEvent palace = (PalaceEvent)event;

            for (PalaceLootTier tier : PalaceLootTier.values()) {
                final String path = "palace-chests." + palaceEventName + ".";

                if (config.get(path + tier.name()) == null) {
                    continue;
                }

                for (String coordValues : config.getStringList(palaceEventName + tier.name())) {
                    final String[] split = coordValues.split(":");
                    final double x, y, z;
                    final String worldName;

                    if (split.length != 4) {
                        Logger.error("Coordinates invalid within '" + coordValues + "' for Palace Loot Chest");
                        continue;
                    }

                    try {
                        x = Double.parseDouble(split[0]);
                        y = Double.parseDouble(split[1]);
                        z = Double.parseDouble(split[2]);
                    } catch (NumberFormatException ex) {
                        Logger.error("Coordinates invalid within '" + coordValues + "' for Palace Loot Chest");
                        continue;
                    }

                    worldName = split[3];

                    final PalaceLootChest chest = new PalaceLootChest(addon, worldName, x, y, z, tier);
                    palace.getLootChests().add(chest);
                }
            }

            Logger.print("Loaded " + palace.getLootChests().size() + " Palace chests for " + palace.getName());
        }
    }

    public PalaceLootChest getPalaceLootChestByBlock(Block block) {
        for (PalaceEvent event : getAddon().getManager().getPalaceEvents()) {
            for (PalaceLootChest chest : event.getLootChests()) {
                if (chest.getX() == block.getX() && chest.getY() == block.getY() && chest.getZ() == block.getZ() && chest.getWorldName().equalsIgnoreCase(block.getWorld().getName())) {
                    return chest;
                }
            }
        }

        return null;
    }

    public ImmutableList<PalaceLootable> getPalaceLootByTier(PalaceLootTier tier) {
        return ImmutableList.copyOf(palaceLootables.stream().filter(lootable -> lootable.getTier().equals(tier)).collect(Collectors.toList()));
    }

    private List<ItemStack> getStandardLoot(int amount) {
        final List<ItemStack> loot = Lists.newArrayList();
        int currentAttempt = 0;
        final int maxAttempts = 1000;

        while (loot.size() < amount && currentAttempt < maxAttempts) {
            for (Lootable item : standardLootables) {
                if (item.pull()) {
                    loot.add(item.getItem());
                }
            }

            currentAttempt += 1;
        }

        return loot;
    }

    private List<ItemStack> getPalaceLoot(PalaceLootTier tier, int amount) {
        final List<ItemStack> loot = Lists.newArrayList();
        int currentAttempt = 0;
        final int maxAttempts = 1000;

        while (loot.size() < amount && currentAttempt < maxAttempts) {
            for (PalaceLootable item : palaceLootables.stream().filter(palaceLoot -> palaceLoot.getTier().equals(tier)).collect(Collectors.toList())) {
                if (item.pull()) {
                    loot.add(item.getItem());
                }
            }

            currentAttempt += 1;
        }

        return loot;
    }
}