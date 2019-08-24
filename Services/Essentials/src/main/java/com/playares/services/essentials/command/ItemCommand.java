package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ItemCommand extends BaseCommand {
    @CommandAlias("item|i|give")
    @CommandPermission("essentials.give")
    @CommandCompletion("@items")
    @Description("Give yourself items")
    @Syntax("<item>")
    public void onItem(Player player, String name) {
        final Material material = Material.getMaterial(name.toUpperCase());

        if (material == null) {
            player.sendMessage(ChatColor.RED + "Item not found");
            return;
        }

        final ItemStack item = new ItemStack(material);
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.YELLOW + "You have been given " + ChatColor.WHITE + StringUtils.capitaliseAllWords(material.name().toLowerCase().replace("_", " ")));
    }

    @CommandAlias("item|i|give")
    @CommandPermission("essentials.give")
    @CommandCompletion("@items")
    @Description("Give yourself items")
    @Syntax("<item> <amount>")
    public void onItem(Player player, String name, int amount) {
        final Material material = Material.getMaterial(name.toUpperCase());

        if (material == null) {
            player.sendMessage(ChatColor.RED + "Item not found");
            return;
        }

        final ItemStack item = new ItemStack(material, amount);
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.YELLOW + "You have been given " + amount + " " + ChatColor.WHITE + StringUtils.capitaliseAllWords(material.name().toLowerCase().replace("_", " ")));
    }

    @HelpCommand
    @Description("View a list of Item Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}