package com.playares.factions.addons.staff.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.factions.addons.staff.StaffAddon;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.essentials.EssentialsService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class DisableVanishItem implements CustomItem {
    @Getter public final StaffAddon addon;

    @Override
    public Material getMaterial() {
        return Material.INK_SACK;
    }

    @Override
    public String getName() {
        return ChatColor.GRAY + "Unvanish";
    }

    @Override
    public short getDurability() {
        return (short)8;
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
            final CustomItemService customItemService = (CustomItemService)addon.getPlugin().getService(CustomItemService.class);
            final EssentialsService essentialsService = (EssentialsService)addon.getPlugin().getService(EssentialsService.class);

            if (!who.hasPermission("factions.mod") && !who.hasPermission("factions.admin")) {
                who.sendMessage(ChatColor.RED + "You do not have permission to use this item");
                return;
            }

            if (essentialsService == null) {
                who.sendMessage(ChatColor.RED + "Failed to obtain Essentials Service");
                return;
            }

            if (!essentialsService.getVanishManager().isVanished(who)) {
                who.sendMessage(ChatColor.RED + "You are already unvanished");
                return;
            }

            customItemService.getItem(EnableVanishItem.class).ifPresent(item -> who.getInventory().setItemInMainHand(item.getItem()));

            essentialsService.getVanishHandler().showPlayer(who, true);
            who.sendMessage(ChatColor.YELLOW + "You are now " + ChatColor.RESET + "visible");
        };
    }
}
