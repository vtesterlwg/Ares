package com.playares.arena.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
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

public final class LeaveDisbandTeamItem implements CustomItem {
    @Getter public final Arenas plugin;

    public LeaveDisbandTeamItem(Arenas plugin) {
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
        return Lists.newArrayList();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            final ArenaPlayer player = plugin.getPlayerManager().getPlayer(who);

            if (player == null) {
                who.sendMessage(ChatColor.RED + "Failed to obtain your profile");
                return;
            }

            final Team team = plugin.getTeamManager().getTeam(player);

            if (team == null) {
                who.sendMessage(ChatColor.RED + "You are not on a team");
                return;
            }

            if (team.isLeader(player)) {
                plugin.getTeamManager().getHandler().disband(player, new SimplePromise() {
                    @Override
                    public void success() {}

                    @Override
                    public void failure(@Nonnull String reason) {
                        who.sendMessage(ChatColor.RED + reason);
                    }
                });

                return;
            }

            plugin.getTeamManager().getHandler().leave(player, new SimplePromise() {
                @Override
                public void success() {}

                @Override
                public void failure(@Nonnull String reason) {
                    who.sendMessage(ChatColor.RED + reason);
                }
            });
        };
    }
}
