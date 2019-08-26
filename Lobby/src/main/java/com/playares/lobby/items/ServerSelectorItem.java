package com.playares.lobby.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.lobby.Lobby;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class ServerSelectorItem implements CustomItem {
    @Getter public final Lobby plugin;

    public Material getMaterial() {
        return Material.COMPASS;
    }

    public String getName() {
        return ChatColor.AQUA + "Join Server";
    }

    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.GRAY + "Right-click while holding this item");
        lore.add(ChatColor.GRAY + "to open a menu that shows all joinable servers");

        return lore;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    public Runnable getRightClick(Player who) {
        return () -> plugin.getSelectorManager().getHandler().openMenu(who);
    }
}
