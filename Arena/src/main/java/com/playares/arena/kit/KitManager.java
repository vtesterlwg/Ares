package com.playares.arena.kit;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.data.Class;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public final class KitManager {
    @Getter public final Arenas plugin;
    @Getter public final KitHandler handler;
    @Getter public final Set<Kit> kits;

    public KitManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new KitHandler(this);
        this.kits = Sets.newConcurrentHashSet();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        final YamlConfiguration config = plugin.getConfig("arena-kits");

        if (config == null) {
            Logger.error("Failed to obtain config 'arena-kits.yml'");
            return;
        }

        if (!kits.isEmpty()) {
            kits.clear();
            Logger.print("Cleared kits while reloading");
        }

        if (config.get("kits") == null) {
            Logger.warn("No kits found in arena-kits file");
            return;
        }

        final PlayerClassService playerClassService = (PlayerClassService)getPlugin().getService(PlayerClassService.class);

        for (String name : config.getConfigurationSection("kits").getKeys(false)) {
            final List<ItemStack> contentItems = (List<ItemStack>)config.getList("kits." + name + ".contents");
            final List<ItemStack> armorItems = (List<ItemStack>)config.getList("kits." + name + ".armor");
            final Class playerClass = (config.get("kits." + name + ".class") != null && playerClassService != null) ? playerClassService.getClassManager().getClassByName(config.getString("kits." + name + ".class")) : null;
            ItemStack[] contents = new ItemStack[contentItems.size()];
            ItemStack[] armor = new ItemStack[armorItems.size()];

            contents = contentItems.toArray(contents);
            armor = armorItems.toArray(armor);

            if (playerClass != null) {
                final ClassKit kit = new ClassKit(plugin, name, contents, armor, playerClass);
                kits.add(kit);
                continue;
            }

            final Kit kit = new Kit(plugin, name, contents, armor);
            kits.add(kit);
        }
    }

    public Kit getKit(String name) {
        return kits.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}