package com.riotmc.factions.addons.stats.lore;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class TrackableSword {
    private final String KILLS_LINE = ChatColor.DARK_RED + "Kills" + ChatColor.RED + ": " + ChatColor.YELLOW;

    @Getter
    public final ItemStack item;

    @Getter @Setter
    public int kills;

    public TrackableSword(ItemStack item) {
        this.item = item;
        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = (meta.getLore() != null ? meta.getLore() : Lists.newArrayList());

        if (lore.isEmpty()) {
            kills = 0;
            update();
            return;
        }

        this.kills = Integer.parseInt(lore.get(1).replace(KILLS_LINE, ""));
    }

    public void update() {
        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = (meta.getLore() != null ? meta.getLore() : Lists.newArrayList());

        lore.clear();
        lore.add(ChatColor.RESET + " ");
        lore.add(KILLS_LINE + kills);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}