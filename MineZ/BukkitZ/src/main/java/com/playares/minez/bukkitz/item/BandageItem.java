package com.playares.minez.bukkitz.item;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.minez.bukkitz.MineZ;
import com.playares.minez.bukkitz.data.MZPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class BandageItem implements CustomItem {
    @Getter public final MineZ plugin;

    public BandageItem(MineZ plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.PAPER;
    }

    @Override
    public String getName() {
        return ChatColor.RESET + "Bandage";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.GRAY + "Right-click to stop bleeding");
        lore.add(ChatColor.GRAY + "Heals 0.5 health");
        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return null;
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            final MZPlayer mzPlayer = plugin.getPlayerManager().getLocalPlayer(who.getUniqueId());

            if (mzPlayer == null) {
                who.sendMessage(ChatColor.RED + "Failed to obtain your profile");
                return;
            }

            if (who.getInventory().getItemInOffHand().isSimilar(getItem())) {
                who.getInventory().setItemInOffHand(null);
            } else {
                who.getInventory().setItemInMainHand(null);
            }

            if (mzPlayer.isBleeding()) {
                mzPlayer.setBleeding(false);
                who.sendMessage(ChatColor.GREEN + "All patched up!");
            }

            if (who.getHealth() < 20.0) {
                double newHealth = who.getHealth() + 1.0;

                if (newHealth > 20.0) {
                    newHealth = 20.0;
                }

                who.setHealth(newHealth);
            }
        };
    }
}