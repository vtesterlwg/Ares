package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("repair")
public final class RepairCommand extends BaseCommand {
    @Subcommand("hand|h")
    @CommandPermission("essentials.repair")
    @Description("Repair the item in your hand")
    @Syntax("hand")
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
    @CommandPermission("essentials.repair")
    @Description("Repair your armor")
    @Syntax("armor")
    public void onRepairArmor(Player player) {

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) {
                return;
            }

            armor.setDurability((short)0);
            player.sendMessage(ChatColor.YELLOW + "Your " + ChatColor.WHITE + armor.getType().getKey().getKey() + ChatColor.YELLOW + " has been repaired");
        }

        Logger.print(player.getName() + " repaired their armor");
    }
}