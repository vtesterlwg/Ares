package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("repair")
public final class RepairCommand extends BaseCommand {
    @CommandAlias("repair")
    @Description("Repair the item in your hand")
    @CommandPermission("essentials.repair")
    public void onRepairHand(Player player) {
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null) {
            player.sendMessage(ChatColor.RED + "You are not holding an item");
            return;
        }

        hand.setDurability((short)0);
        player.sendMessage(ChatColor.YELLOW + "Item in your hand has been repaired");
        Logger.print(player.getName() + " repaired the item in their hand");
    }

    @Subcommand("armor|a")
    @Description("Repair")
    @CommandPermission("essentials.repair")
    public void onRepairArmor(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) {
                return;
            }

            armor.setDurability((short)0);
            player.sendMessage(ChatColor.YELLOW + "Your " + ChatColor.WHITE + armor.getType().name().replace("_", " ") + ChatColor.YELLOW + " has been repaired");
        }

        Logger.print(player.getName() + " repaired their armor");
    }

    @HelpCommand
    @Description("View a list of Repair Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}