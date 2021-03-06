package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.util.Players;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HealCommand extends BaseCommand {
    @CommandAlias("heal")
    @CommandPermission("essentials.heal")
    @Description("Heal yourself")
    @Syntax("/heal")
    public void onCommand(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.getActivePotionEffects().clear();
        Players.resetFlySpeed(player);
        Players.resetWalkSpeed(player);

        player.sendMessage(ChatColor.YELLOW + "You have been healed");
    }

    @CommandAlias("heal")
    @CommandPermission("essentials.heal")
    @CommandCompletion("@players")
    @Description("Heal yourself")
    @Syntax("<player>")
    public void onCommand(CommandSender sender, String name) {
        final Player player = Bukkit.getPlayer(name);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.getActivePotionEffects().clear();
        Players.resetFlySpeed(player);
        Players.resetWalkSpeed(player);

        player.sendMessage(ChatColor.YELLOW + "You have been healed");
        sender.sendMessage(ChatColor.YELLOW + "You have healed " + ChatColor.WHITE + player.getName());
    }

    @HelpCommand
    @Description("View a list of Heal Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}
