package com.riotmc.services.essentials.data.kit;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.ItemSerializer;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class KitManager {
    @Getter
    public final RiotPlugin plugin;

    @Getter
    public final Set<Kit> kits;

    @Getter
    public final YamlConfiguration kitsConfig;

    public KitManager(RiotPlugin plugin) {
        this.plugin = plugin;
        this.kits = Sets.newConcurrentHashSet();
        this.kitsConfig = plugin.getConfig("kits");

        if (kitsConfig.getConfigurationSection("kits") == null) {
            Logger.warn("No kits found...");
            return;
        }

        for (String kitNames : kitsConfig.getConfigurationSection("kits").getKeys(false)) {
            final ItemStack[] contents = ItemSerializer.decodeItemStacks(kitsConfig.getString("kits." + kitNames + ".contents"));
            final ItemStack[] armor = ItemSerializer.decodeItemStacks(kitsConfig.getString("kits." + kitNames + ".armor"));

            final Kit kit = new Kit(kitNames, contents, armor);
            this.kits.add(kit);
        }
    }

    public Kit getKit(String name) {
        return kits.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void deleteKit(Kit kit) {
        kitsConfig.set("kits." + kit.getName(), null);
        plugin.saveConfig("kits", kitsConfig);
    }

    public void saveKits() {
        if (kits.isEmpty()) {
            kitsConfig.set("kits", null);
            plugin.saveConfig("kits", kitsConfig);
            return;
        }

        for (Kit kit : kits) {
            kitsConfig.set("kits." + kit.getName() + ".contents", ItemSerializer.encodeItemStacks(kit.getContents()));
            kitsConfig.set("kits." + kit.getName() + ".armor", ItemSerializer.encodeItemStacks(kit.getArmor()));
        }

        plugin.saveConfig("kits", kitsConfig);
    }
}