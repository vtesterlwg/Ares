package com.playares.lobby.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.lobby.Lobby;
import com.playares.lobby.queue.ServerQueue;
import com.playares.lobby.util.LobbyUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class LeaveQueueItem implements CustomItem {
    @Getter public final Lobby plugin;

    @Override
    public Material getMaterial() {
        return Material.BARRIER;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Leave Queue";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.GRAY + "Right-click while holding this");
        lore.add(ChatColor.GRAY + "item to leave the queue you are");
        lore.add(ChatColor.GRAY + "currently waiting in.");
        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            final ServerQueue queue = plugin.getQueueManager().getQueue(who);

            if (queue != null) {
                queue.remove(who.getUniqueId());
                who.sendMessage(ChatColor.RED + "You have left the queue to join " + queue.getServer().getDisplayName());
            }

            who.getInventory().clear();

            LobbyUtils.giveStandardItems(plugin, who);

            if (who.hasPermission("lobby.premium") || who.hasPermission("lobby.staff")) {
                LobbyUtils.givePremiumItems(who);
            }
        };
    }
}