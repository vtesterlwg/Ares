package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ClearCommand extends BaseCommand {
    @CommandAlias("clear")
    @Description("Clear your inventory")
    @CommandPermission("essentials.clear")
    public void onCommand(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.sendMessage(ChatColor.YELLOW + "Your inventory has been cleared");
    }

    @CommandAlias("clear")
    @Description("Clear a players inventory")
    @CommandPermission("essentials.clear.other")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onCommand(CommandSender sender, String name) {
        final Player player = Bukkit.getPlayer(name);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.sendMessage(ChatColor.YELLOW + "Your inventory has been cleared by " + ChatColor.WHITE + sender.getName());
    }

    @HelpCommand
    @Description("View a list of Clear Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}