package com.playares.arena.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.item.custom.CustomItem;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public final class LeaveTeamItem implements CustomItem {
    @Getter
    public final Arenas plugin;

    public LeaveTeamItem(Arenas plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.BARRIER;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Leave/Disband Team";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.GRAY + "Right-click while holding this item");
        lore.add(ChatColor.GRAY + "to leave/disband your team");

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

            if (player.getTeam().getLeader().equals(player)) {
                plugin.getTeamHandler().disbandTeam(player, new SimplePromise() {
                    @Override
                    public void success() {
                        plugin.getPlayerHandler().giveLobbyItems(player);
                    }

                    @Override
                    public void failure(@Nonnull String reason) {
                        who.sendMessage(ChatColor.RED + reason);
                    }
                });

                return;
            }

            plugin.getTeamHandler().leaveTeam(player, new SimplePromise() {
                @Override
                public void success() {
                    plugin.getPlayerHandler().giveLobbyItems(player);
                    player.getPlayer().sendMessage(ChatColor.GREEN + "You have left your team");
                }

                @Override
                public void failure(@Nonnull String reason) {
                    who.sendMessage(ChatColor.RED + reason);
                }
            });
        };
    }
}