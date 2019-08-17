package com.playares.services.essentials.data.kit;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public final class KitManager {
    @Getter public final AresPlugin plugin;
    @Getter public final Set<Kit> kits;
    @Getter public final YamlConfiguration kitsConfig;

    @SuppressWarnings("unchecked")
    public KitManager(AresPlugin plugin) {
        this.plugin = plugin;
        this.kits = Sets.newConcurrentHashSet();
        this.kitsConfig = plugin.getConfig("kits");

        if (kitsConfig.getConfigurationSection("kits") == null) {
            Logger.warn("No kits found...");
            return;
        }

        for (String kitNames : kitsConfig.getConfigurationSection("kits").getKeys(false)) {
            final List<ItemStack> contentItems = (List<ItemStack>)kitsConfig.getList("kits." + kitNames + ".contents");
            final List<ItemStack> armorItems = (List<ItemStack>)kitsConfig.getList("kits." + kitNames + ".armor");
            ItemStack[] contents = new ItemStack[contentItems.size()];
            ItemStack[] armor = new ItemStack[armorItems.size()];

            contents = contentItems.toArray(contents);
            armor = armorItems.toArray(armor);

            final Kit kit = new Kit(kitNames, contents, armor);

            kits.add(kit);
        }

        Logger.print("Loaded " + kits.size() + " kits");
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
            kitsConfig.set("kits." + kit.getName() + ".contents", kit.getContents());
            kitsConfig.set("kits." + kit.getName() + ".armor", kit.getArmor());
        }

        plugin.saveConfig("kits", kitsConfig);
    }
}