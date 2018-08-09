package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
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

        player.sendMessage(ChatColor.GREEN + "Applied name '" + converted + ChatColor.GREEN + "' to item");
        Logger.print(player.getName() + " renamed the item in their hand to " + converted);
    }
}