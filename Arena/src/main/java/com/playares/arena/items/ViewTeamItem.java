package com.playares.arena.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.bukkit.item.custom.CustomItem;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class ViewTeamItem implements CustomItem {
    @Getter
    public final Arenas plugin;

    public ViewTeamItem(Arenas plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.PLAYER_HEAD;
    }

    @Override
    public String getName() {
        return ChatColor.GREEN + "View Teams";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.GRAY + "Right-click while holding this item");
        lore.add(ChatColor.GRAY + "to view a list of other available teams");
        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            final ArenaPlayer player = plugin.getPlayerManager().getPlayer(who.getUniqueId());

            if (player == null) {
                who.sendMessage(ChatColor.RED + "Failed to obtain your profile");
                return;
            }

            if (player.getTeam() == null) {
                who.sendMessage(ChatColor.RED + "You are not on a team");
                return;
            }

            plugin.getMenuHandler().openTeamMenu(who);
        };
    }
}