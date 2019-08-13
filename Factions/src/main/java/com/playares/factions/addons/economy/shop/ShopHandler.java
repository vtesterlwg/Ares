package com.playares.factions.addons.economy.shop;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.economy.EconomyAddon;
import com.playares.factions.util.ItemUtils;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;

public final class ShopHandler {
    @Getter public final EconomyAddon addon;

    public ShopHandler(EconomyAddon addon) {
        this.addon = addon;
    }

    boolean isInvalidItem(String name) {
        return (ItemUtils.getItemByName(name) == null && ItemUtils.getItemById(name) == null && ItemUtils.getCustomItem(addon.getPlugin(), name) == null);
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