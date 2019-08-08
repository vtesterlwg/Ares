package com.playares.factions.addons.economy.shop;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.economy.EconomyAddon;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public final class ShopHandler {
    @Getter public final EconomyAddon addon;

    public ShopHandler(EconomyAddon addon) {
        this.addon = addon;
    }

    public ItemStack getByName(String name) {
        if (name.contains(":")) {
            final String materialName = name.split(":")[0].toUpperCase();
            final String idAsString = name.split(":")[1];
            final int id;

            try {
                id = Integer.parseInt(idAsString);
            } catch (NumberFormatException ex) {
                return null;
            }

            final Material material = Material.getMaterial(materialName);

            if (material != null) {
                return new ItemBuilder()
                        .setMaterial(material)
                        .setData((short)id)
                        .build();
            }

            return null;
        }

        final Material material = Material.getMaterial(name.toUpperCase());

        if (material == null) {
            return null;
        }

        return new ItemBuilder()
                .setMaterial(material)
                .build();
    }

    ItemStack getById(String value) {
        if (value.contains(":")) {
            final String itemIdAsString = value.split(":")[0];
            final String itemDataAsString = value.split(":")[1];
            final int id;
            final int data;

            try {
                id = Integer.parseInt(itemIdAsString);
                data = Integer.parseInt(itemDataAsString);
            } catch (NumberFormatException ex) {
                return null;
            }

            final Material material = Material.getMaterial(id);

            if (material != null) {
                return new ItemBuilder()
                        .setMaterial(material)
                        .setData((short)data)
                        .build();
            }

            return null;
        }

        final int id;

        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }

        final Material material = Material.getMaterial(id);

        return new ItemBuilder()
                .setMaterial(material)
                .build();
    }

    CustomItem getByCustomItem(String name) {
        final CustomItemService service = (CustomItemService)getAddon().getPlugin().getService(CustomItemService.class);

        if (service == null) {
            return null;
        }

        final Optional<CustomItem> item = service.getItem(name);

        return item.orElse(null);
    }

    boolean isValidItem(String name) {
        return (getByName(name) != null || getById(name) != null || getByCustomItem(name) != null);
    }

    void setInvalidSign(SignChangeEvent event) {
        event.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Invalid Shop Sign");
        event.setLine(1, "");
        event.setLine(2, "");
        event.setLine(3, "");
    }

    void flashPurchase(Player player, Sign sign, int amount, double price, String name) {
        final List<String> lines = Lists.newArrayList();

        lines.add("Purchased x" + amount);
        lines.add(ChatColor.GOLD + name);
        lines.add("for " + ChatColor.GREEN + "$" + String.format("%.2f", price));
        lines.add("");

        Players.sendSignChange(player, sign.getLocation(), lines.toArray(new String[0]));

        new Scheduler(getAddon().getPlugin()).sync(() -> Players.sendSignChange(player, sign.getLocation(), sign.getLines())).delay(20L).run();
    }

    void flashSale(Player player, Sign sign, int amount, double value, String name) {
        final List<String> lines = Lists.newArrayList();

        lines.add("Sold x" + amount);
        lines.add(ChatColor.GOLD + name);
        lines.add("for " + ChatColor.GREEN + "$" + String.format("%.2f", value));
        lines.add("");

        Players.sendSignChange(player, sign.getLocation(), lines.toArray(new String[0]));

        new Scheduler(getAddon().getPlugin()).sync(() -> Players.sendSignChange(player, sign.getLocation(), sign.getLines())).delay(20L).run();
    }
}