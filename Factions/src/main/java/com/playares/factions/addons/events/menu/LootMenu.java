package com.playares.factions.addons.events.menu;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.loot.Lootable;
import com.playares.factions.addons.events.loot.palace.PalaceLootable;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

public final class LootMenu extends Menu {
    @Getter public final EventsAddon addon;

    public LootMenu(EventsAddon addon, @Nonnull AresPlugin plugin, @Nonnull Player player, @Nonnull String title, int rows) {
        super(plugin, player, title, rows);
        this.addon = addon;
    }

    public void populate(List<Lootable> loot) {
        int pos = 0;
        loot.sort(Comparator.comparingInt(Lootable::getTotalPulls));

        for (Lootable item : loot) {
            if (item == null || item.getItem() == null || item.getItem().getItemMeta() == null) {
                continue;
            }

            final ItemStack itemStack = item.getItem();
            final int requiredPulls = item.getRequiredPull();
            final int totalPulls = item.getTotalPulls();
            final ItemMeta meta = itemStack.getItemMeta();
            final List<String> lore = (meta.getLore() != null) ? meta.getLore() : Lists.newArrayList();

            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GREEN + "Chance: " + requiredPulls + ":" + totalPulls);

            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            addItem(new ClickableItem(itemStack, pos, null));

            pos++;
        }
    }

    public void populatePalace(List<PalaceLootable> loot) {
        int pos = 0;
        loot.sort(Comparator.comparingInt(PalaceLootable::getTotalPulls));

        for (Lootable item : loot) {
            if (item == null || item.getItem() == null || item.getItem().getItemMeta() == null) {
                continue;
            }

            final ItemStack itemStack = item.getItem();
            final int requiredPulls = item.getRequiredPull();
            final int totalPulls = item.getTotalPulls();
            final ItemMeta meta = itemStack.getItemMeta();
            final List<String> lore = (meta.getLore() != null) ? meta.getLore() : Lists.newArrayList();

            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GREEN + "Chance: " + requiredPulls + ":" + totalPulls);

            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            addItem(new ClickableItem(itemStack, pos, null));

            pos++;
        }
    }
}