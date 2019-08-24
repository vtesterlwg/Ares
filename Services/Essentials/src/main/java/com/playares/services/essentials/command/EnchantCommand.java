package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.remap.RemappedEnchantment;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("enchant")
public final class EnchantCommand extends BaseCommand {
    @CommandAlias("enchant")
    @CommandPermission("essentials.enchant")
    @CommandCompletion("@enchantments")
    @Syntax("[-a] <enchantment> <level>")
    public void onEnchant(Player player, String name, int level) {
        final Enchantment enchantment = RemappedEnchantment.getEnchantmentByName(name);
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (enchantment == null) {
            player.sendMessage(ChatColor.RED + "Enchantment not found");
            return;
        }

        if (hand == null) {
            player.sendMessage(ChatColor.RED + "You are not holding an item in your main hand");
            return;
        }

        hand.addUnsafeEnchantment(enchantment, level);
        player.sendMessage(ChatColor.YELLOW + "Item in your hand has been enchanted with " + ChatColor.RESET + enchantment.getName() + " " + level);
        Logger.print(player.getName() + " applied " + enchantment.getName() + " " + level + " to their " + hand.getType().name());;
    }

    @CommandAlias("enchant")
    @CommandPermission("essentials.enchant")
    @CommandCompletion("@enchantments")
    @Syntax("[-a] <enchantment> <level>")
    public void onEnchant(Player player, @Values("-a") String value, String name, int level) {
        final Enchantment enchantment = RemappedEnchantment.getEnchantmentByName(name);
        int count = 0;

        if (enchantment == null) {
            player.sendMessage(ChatColor.RED + "Enchantment not found. Type " + ChatColor.YELLOW + "/enchant list" + ChatColor.RED + " to view a list of enchantments");
            return;
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) {
                continue;
            }

            armor.addUnsafeEnchantment(enchantment, level);
            count += 1;
        }

        player.sendMessage(ChatColor.YELLOW + "Applied " + ChatColor.WHITE + enchantment.getName() + " " + level + ChatColor.YELLOW + " to " + ChatColor.WHITE + count + ChatColor.YELLOW + " pieces of armor");
        Logger.print(player.getName() + " applied " + enchantment.getName() + " " + level + " to their armor");
    }

    @HelpCommand
    @Description("View a list of Enchant Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}