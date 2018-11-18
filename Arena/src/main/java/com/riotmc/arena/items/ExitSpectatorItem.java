package com.riotmc.arena.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

public final class ExitSpectatorItem implements CustomItem {
    @Getter
    public final Arenas plugin;

    public ExitSpectatorItem(Arenas plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.BARRIER;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Exit Spectator Mode";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.YELLOW + "Right-click while holding this item");
        lore.add(ChatColor.YELLOW + "to return to the lobby");

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

            plugin.getSpectatorHandler().stopSpectating(profile, new SimplePromise() {
                @Override
                public void success() {
                    who.sendMessage(ChatColor.GREEN + "You are no longer spectating");
                }

                @Override
                public void failure(@Nonnull String reason) {
                    who.sendMessage(ChatColor.RED + reason);
                }
            });
        };
    }
}