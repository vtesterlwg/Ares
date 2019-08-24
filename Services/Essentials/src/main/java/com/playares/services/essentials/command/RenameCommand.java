package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class RenameCommand extends BaseCommand {
    @CommandAlias("rename")
    @CommandPermission("essentials.rename")
    @Description("Rename the item in your hand")
    @Syntax("<name>")
    public void onRename(Player player, String name) {
        final String converted = ChatColor.translateAlternateColorCodes('&', name);
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null) {
            player.sendMessage(ChatColor.RED + "You are not holding an item");
            return;
        }

        final ItemMeta meta = hand.getItemMeta();
        meta.setDisplayName(converted);
        hand.setItemMeta(meta);

        player.sendMessage(ChatColor.YELLOW + "The item in your hand has been renamed to " + converted);
        Logger.print(player.getName() + " renamed the item in their hand to " + converted);
    }

    @HelpCommand
    @Description("View a list of Rename Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}