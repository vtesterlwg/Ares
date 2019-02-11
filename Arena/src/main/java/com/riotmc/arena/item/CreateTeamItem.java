package com.riotmc.arena.item;

import com.google.common.collect.Lists;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.player.ArenaPlayer;
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

public final class CreateTeamItem implements CustomItem {
    @Getter public final Arenas plugin;

    public CreateTeamItem(Arenas plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.CLOCK;
    }

    @Override
    public String getName() {
        return ChatColor.BLUE + "Create Team";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.AQUA + "Right-click while holding this item");
        lore.add(ChatColor.AQUA + "to create a new team!");

        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return null;
    }

    @Override
    public Runnable getLeftClick(Player who) {
        return null;
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            final ArenaPlayer player = plugin.getPlayerManager().getPlayer(who);

            if (player == null) {
                who.sendMessage("Failed to obtain your profile");
                return;
            }

            if (plugin.getTeamManager().getTeam(player) != null) {
                who.sendMessage(ChatColor.RED + "You are already on a team");
                return;
            }

            if (!player.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
                who.sendMessage(ChatColor.RED + "You are not in the lobby");
                return;
            }

            plugin.getTeamManager().getHandler().create(player, new SimplePromise() {
                @Override
                public void success() {
                    plugin.getPlayerManager().getHandler().giveItems(player);
                    who.sendMessage(ChatColor.GREEN + "Team created!");
                }

                @Override
                public void failure(@Nonnull String reason) {
                    who.sendMessage(ChatColor.RED + reason);
                }
            });
        };
    }
}