package com.playares.arena.items;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import com.playares.commons.bukkit.item.custom.CustomItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class TeamStatusItem implements CustomItem {
    final Arenas plugin;

    public TeamStatusItem(Arenas plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.BOOK;
    }

    @Override
    public String getName() {
        return ChatColor.YELLOW + "Team Status";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.YELLOW + "Right-click while holding this item");
        lore.add(ChatColor.YELLOW + "to view info about your current team");
        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(who.getUniqueId());

            if (profile == null) {
                who.sendMessage(ChatColor.RED + "Failed to obtain your profile");
                return;
            }

            if (profile.getTeam() == null) {
                who.sendMessage(ChatColor.RED + "You are not on a team");
                return;
            }

            final Team team = profile.getTeam();
            final List<String> memberNames = Lists.newArrayList();

            team.getMembers().forEach(member -> memberNames.add(member.getUsername()));

            who.sendMessage(ChatColor.BLUE + team.getName());
            who.sendMessage(ChatColor.GOLD + "Leader" + ChatColor.YELLOW + ": " + ChatColor.WHITE + team.getLeader().getUsername());
            who.sendMessage(ChatColor.GOLD + "Open" + ChatColor.YELLOW + ": " + ChatColor.WHITE + team.isOpen());
            who.sendMessage(ChatColor.GOLD + "Status" + ChatColor.YELLOW + ": " + ChatColor.WHITE + StringUtils.capitalize(team.getStatus().name().toLowerCase()).replace("_", " "));
            who.sendMessage(ChatColor.GOLD + "Roster" + ChatColor.YELLOW + ": " + ChatColor.WHITE + Joiner.on(ChatColor.WHITE + ", ").join(memberNames));
        };
    }
}
