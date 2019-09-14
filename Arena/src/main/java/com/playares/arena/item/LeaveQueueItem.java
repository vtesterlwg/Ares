package com.playares.arena.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.SearchingPlayer;
import com.playares.commons.bukkit.item.custom.CustomItem;
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
    @Getter public final Arenas plugin;

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
        return Lists.newArrayList();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(who);

            if (profile == null) {
                return;
            }

            final SearchingPlayer current = plugin.getQueueManager().getCurrentSearch(who);

            if (current != null) {
                plugin.getQueueManager().getSearchingPlayers().remove(current);
                who.sendMessage(ChatColor.RED + "Left Queue: " + ChatColor.RESET + current.getQueueType().getDisplayName());
            }

            plugin.getPlayerManager().getHandler().giveItems(profile);
        };
    }
}