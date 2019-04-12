package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Items;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("enchant")
public final class EnchantCommand extends BaseCommand {
    @Subcommand("hand|h")
    @CommandPermission("essentials.enchant")
    @CommandCompletion("@enchantments")
    @Syntax("<enchantment> <level>")
    public void onEnchantHand(Player player, String enchant, int level) {
        final Enchantment enchantment = Items.getEnchantmentByName(enchant);
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (enchantment == null) {
            player.sendMessage(ChatColor.RED + "Enchantment not found");
            return;
        }

        if (hand == null) {
            player.sendMessage(ChatColor.RED + "You are not holding an item");
            return;
        }

        hand.addUnsafeEnchantment(enchantment, level);
        player.sendMessage(ChatColor.YELLOW + "Item in your hand has been enchanted with " + ChatColor.WHITE + enchantment.getKey().getKey() + " " + level);
        Logger.print(player.getName() + " applied " + enchantment.getKey().getKey() + " " + level + " to their " + hand.getType().name());
    }

    @Subcommand("armor|a")
    @CommandPermission("essentials.enchant")
    @CommandCompletion("@enchantments")
    @Syntax("<enchantment> <level>")
    public void onEnchantArmor(Player player, String enchant, int level) {
        final Enchantment enchantment = Items.getEnchantmentByName(enchant);

        if (enchantment == null) {
            player.sendMessage(ChatColor.RED + "Enchantment not found");
            return;
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) {
                continue;
            }

            armor.addUnsafeEnchantment(enchantment, level);
            player.sendMessage(ChatColor.YELLOW + "Your " + ChatColor.WHITE + armor.getI18NDisplayName() + ChatColor.YELLOW + " has been enchanted with " + ChatColor.WHITE + enchantment.getKey().getKey() + " " + level);
        }

        Logger.print(player.getName() + " applied " + enchantment.getName() + " to their armor");
    }
}