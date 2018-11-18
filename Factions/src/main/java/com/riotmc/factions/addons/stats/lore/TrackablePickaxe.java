package com.riotmc.factions.addons.stats.lore;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class TrackablePickaxe {
    private final String COAL_LINE = ChatColor.DARK_GRAY + "Coal" + ChatColor.GRAY + ": " + ChatColor.YELLOW;
    private final String IRON_LINE = ChatColor.GRAY + "Iron" + ChatColor.WHITE + ": " + ChatColor.YELLOW;
    private final String REDSTONE_LINE = ChatColor.DARK_RED + "Redstone" + ChatColor.RED + ": " + ChatColor.YELLOW;
    private final String LAPIS_LINE = ChatColor.DARK_BLUE + "Lapis" + ChatColor.BLUE + ": " + ChatColor.YELLOW;
    private final String GOLD_LINE = ChatColor.GOLD + "Gold" + ChatColor.YELLOW + ": ";
    private final String DIAMOND_LINE = ChatColor.AQUA + "Diamond" + ChatColor.WHITE + ": " + ChatColor.YELLOW;
    private final String EMERALD_LINE = ChatColor.DARK_GREEN + "Emerald" + ChatColor.GREEN + ": " + ChatColor.YELLOW;

    @Getter
    public final ItemStack item;

    @Getter @Setter
    public int coal, iron, redstone, lapis, gold, diamond, emerald;

    public TrackablePickaxe(ItemStack item) {
        this.item = item;
        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = (meta.getLore() != null ? meta.getLore() : Lists.newArrayList());

        if (lore.isEmpty()) {
            coal = 0;
            iron = 0;
            redstone = 0;
            lapis = 0;
            gold = 0;
            diamond = 0;
            emerald = 0;
            update();
            return;
        }

        coal = Integer.parseInt(lore.get(1).replace(COAL_LINE, ""));
        iron = Integer.parseInt(lore.get(2).replace(IRON_LINE, ""));
        redstone = Integer.parseInt(lore.get(3).replace(REDSTONE_LINE, ""));
        lapis = Integer.parseInt(lore.get(4).replace(LAPIS_LINE, ""));
        gold = Integer.parseInt(lore.get(5).replace(GOLD_LINE, ""));
        diamond = Integer.parseInt(lore.get(6).replace(DIAMOND_LINE, ""));
        emerald = Integer.parseInt(lore.get(7).replace(EMERALD_LINE, ""));
    }

    public void update() {
        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = (meta.getLore() != null ? meta.getLore() : Lists.newArrayList());

        lore.clear();

        lore.add(ChatColor.RESET + " ");
        lore.add(COAL_LINE + coal);
        lore.add(IRON_LINE + iron);
        lore.add(REDSTONE_LINE + redstone);
        lore.add(LAPIS_LINE + lapis);
        lore.add(GOLD_LINE + gold);
        lore.add(DIAMOND_LINE + diamond);
        lore.add(EMERALD_LINE + emerald);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}