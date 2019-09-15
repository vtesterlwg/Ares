package com.playares.arena.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.item.custom.CustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class LeaveSpectatorItem implements CustomItem {
    @Getter public final Arenas plugin;

    @Override
    public Material getMaterial() {
        return Material.BARRIER;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Stop Spectating";
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
        return () -> plugin.getSpectateManager().getHandler().stopSpectating(who, new SimplePromise() {
            @Override
            public void success() {
                who.sendMessage(ChatColor.GREEN + "You are no longer spectating");
            }

            @Override
            public void failure(@Nonnull String reason) {
                who.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}