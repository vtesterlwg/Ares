package com.riotmc.arena.item;

import com.google.common.collect.Lists;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.arena.team.Team;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.item.custom.CustomItem;
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
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.YELLOW + "Right-click while holding this item");
        lore.add(ChatColor.YELLOW + "to leave/disband your team");

        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return null;
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
