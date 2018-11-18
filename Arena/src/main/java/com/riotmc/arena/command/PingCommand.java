package com.riotmc.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class PingCommand extends BaseCommand {
    @CommandAlias("ping")
    @Description("View your connection to the server")
    public void onPing(Player player) {
        final int ms = ((CraftPlayer)player).getHandle().ping;
        final ChatColor color;

        if (ms <= 70) {
            color = ChatColor.GREEN;
        } else if (ms <= 120) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.RED;
        }

        player.sendMessage(ChatColor.YELLOW + "Your connection: " + color + ms + "ms");
    }

    @CommandAlias("ping")
    @CommandCompletion("@players")
    @Description("View a players connection to the server")
    @Syntax("<player>")
    public void onPing(Player player, String name) {
        final Player target = Bukkit.getPlayer(name);

        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        final int ms = ((CraftPlayer)target).getHandle().ping;
        final ChatColor color;

        if (ms <= 70) {
            color = ChatColor.GREEN;
        } else if (ms <= 120) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.RED;
        }

        player.sendMessage(ChatColor.YELLOW + target.getName() + "'s connection: " + color + ms + "ms");
    }
}