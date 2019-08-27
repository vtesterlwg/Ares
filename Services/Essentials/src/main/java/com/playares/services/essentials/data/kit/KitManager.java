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

    public KitManager(AresPlugin plugin) {
        this.plugin = plugin;
        this.kits = Sets.newConcurrentHashSet();

        getPlugin().registerListener(new KitSignListener(this));
    }

    @SuppressWarnings("unchecked")
    public void load() {
        final YamlConfiguration config = getPlugin().getConfig("kits");

        if (!kits.isEmpty()) {
            kits.clear();
            Logger.warn("Cleared kits while reloading " + getPlugin().getName());
        }

        if (config.getConfigurationSection("kits") == null) {
            Logger.warn("No kits found...");
            return;
        }

        for (String kitNames : config.getConfigurationSection("kits").getKeys(false)) {
            final List<ItemStack> contentItems = (List<ItemStack>)config.getList("kits." + kitNames + ".contents");
            final List<ItemStack> armorItems = (List<ItemStack>)config.getList("kits." + kitNames + ".armor");
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
        final YamlConfiguration config = getPlugin().getConfig("kits");
        config.set("kits." + kit.getName(), null);
        plugin.saveConfig("kits", config);
    }

    public void saveKits() {
        final YamlConfiguration config = getPlugin().getConfig("kits");

        if (kits.isEmpty()) {
            config.set("kits", null);
            plugin.saveConfig("kits", config);
            return;
        }

        for (Kit kit : kits) {
            config.set("kits." + kit.getName() + ".contents", kit.getContents());
            config.set("kits." + kit.getName() + ".armor", kit.getArmor());
        }

        plugin.saveConfig("kits", config);
    }
}