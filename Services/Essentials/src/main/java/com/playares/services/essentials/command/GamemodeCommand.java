package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GamemodeCommand extends BaseCommand {
    private void updateGamemode(Player player, String mode, SimplePromise promise) {
        if (mode.startsWith("su") || mode.startsWith("0")) {
            player.setGameMode(GameMode.SURVIVAL);
            promise.success();
            return;
        }

        if (mode.startsWith("c") || mode.startsWith("1")) {
            player.setGameMode(GameMode.CREATIVE);
            promise.success();
            return;
        }

        if (mode.startsWith("a") || mode.startsWith("2")) {
            player.setGameMode(GameMode.ADVENTURE);
            promise.success();
            return;
        }

        if (mode.startsWith("sp") || mode.startsWith("3")) {
            player.setGameMode(GameMode.SPECTATOR);
            promise.success();
            return;
        }

        promise.failure("Gamemode not found");
    }

    @CommandAlias("gamemode|gm")
    @CommandPermission("essentials.gamemode")
    @Description("Change gamemodes")
    @Syntax("<mode>")
    public void onCommand(final Player player, String mode) {
        updateGamemode(player, mode, new SimplePromise() {
            public void success() {
                player.sendMessage(ChatColor.YELLOW + "Your gamemode has been updated to " + ChatColor.WHITE + StringUtils.capitalize(player.getGameMode().name().toLowerCase().replace("_", " ")));
            }

            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("gamemode|gm")
    @CommandPermission("essentials.gamemode.others")
    @CommandCompletion("@players")
    @Description("Change other player's gamemodes")
    @Syntax("<player> <mode>")
    public void onCommand(final CommandSender sender, final String player, String mode) {
        final Player toUpdate = Bukkit.getPlayer(player);

        if (toUpdate == null) {
            sender.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        updateGamemode(toUpdate, mode, new SimplePromise() {
            public void success() {
                sender.sendMessage(ChatColor.YELLOW + "You have updated " + ChatColor.WHITE + toUpdate.getName() + ChatColor.YELLOW + "'s gamemode to " + ChatColor.WHITE + StringUtils.capitalize(toUpdate.getGameMode().name().toLowerCase().replace("_", " ")));
            }

            public void failure(String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}