package com.riotmc.arena.mode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.loadout.Loadout;
import com.riotmc.arena.mode.cont.StandardMode;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.serialize.InventorySerializer;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModeManager {
    @Nonnull @Getter
    public final Arenas plugin;

    @Nonnull @Getter
    public final Set<Mode> modes;

    @Nonnull @Getter
    public final YamlConfiguration config;

    public ModeManager(@Nonnull Arenas plugin) {
        this.plugin = plugin;
        this.modes = Sets.newConcurrentHashSet();
        this.config = plugin.getConfig("modes");

        for (String modeName : config.getConfigurationSection("modes").getKeys(false)) {
            final ItemStack icon = InventorySerializer.decodeItemStack(config.getString("modes." + modeName + ".icon"));
            final List<String> loadoutNames = config.getStringList("modes." + modeName + ".loadouts");
            final List<Loadout> loadouts = Lists.newArrayList();

            for (String loadoutName : loadoutNames) {
                final Loadout loadout = plugin.getLoadoutManager().getLoadout(loadoutName);

                if (loadout == null) {
                    Logger.error("Failed to obtain loadout '" + loadoutName + "' for mode '" + modeName + "'");
                    continue;
                }

                loadouts.add(loadout);
            }

            final StandardMode mode = new StandardMode(modeName);

            mode.setIcon(icon);
            mode.getLoadouts().addAll(loadouts);

            modes.add(mode);
        }

        Logger.print("Loaded " + modes.size() + " Modes");
    }

    @Nullable
    public Mode getMode(@Nonnull String name) {
        return modes.stream().filter(mode -> mode.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Nonnull
    public ImmutableList<Mode> getConfiguredModes() {
        return ImmutableList.copyOf(modes.stream().filter(Mode::isConfigured).collect(Collectors.toList()));
    }

    @Nonnull
    public ImmutableList<Mode> getSortedConfiguredModes() {
        final List<Mode> configured = Lists.newArrayList(getConfiguredModes());
        configured.sort(Comparator.comparing(Mode::getName));
        return ImmutableList.copyOf(configured);
    }

    public void saveMode(@Nonnull Mode mode) {
        if (!mode.isConfigured()) {
            return;
        }

        final List<String> loadoutNames = Lists.newArrayList();

        mode.getLoadouts().forEach(loadout -> loadoutNames.add(loadout.getName()));

        config.set("modes." + mode.getName() + ".icon", InventorySerializer.encodeItemStackToString(mode.getIcon()));
        config.set("modes." + mode.getName() + ".loadouts", loadoutNames);

        plugin.saveConfig("modes", config);
    }

    public void deleteMode(@Nonnull Mode mode) {
        config.set("modes." + mode.getName(), null);
        plugin.saveConfig("modes", config);
    }
}
