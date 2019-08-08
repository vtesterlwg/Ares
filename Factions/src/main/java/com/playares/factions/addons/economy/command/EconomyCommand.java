package com.playares.factions.addons.economy.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.economy.EconomyAddon;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class EconomyCommand extends BaseCommand {
    @Getter public final EconomyAddon addon;

    public EconomyCommand(EconomyAddon addon) {
        this.addon = addon;
    }

    @CommandAlias("balance|bal")
    @Description("View your current balance")
    @Syntax("<player>")
    public void onBalance(Player player) {
        addon.getHandler().getBalance(player.getUniqueId(), amount -> player.sendMessage(ChatColor.YELLOW + "Your balance: " + ChatColor.GREEN + "$" + String.format("%.2f", amount)));
    }

    @CommandAlias("balance|bal")
    @Description("View a player's current balance")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onBalance(CommandSender sender, String username) {
        addon.getHandler().getBalance(username, amount -> sender.sendMessage(ChatColor.YELLOW + "This player has " + ChatColor.GREEN + "$" + String.format("%.2f", amount)));
    }

    @CommandAlias("pay")
    @Description("Transfer money from your balance to another player")
    @Syntax("<player> <amount>")
    @CommandCompletion("@players")
    public void onPay(CommandSender sender, String receiver, double value) {
        if (value <= 0.0) {
            sender.sendMessage(ChatColor.RED + "Minimum payment amount: $" + String.format("%.2f", getAddon().getMinPayAmount()));
            return;
        }

        final double amount = Math.round((value * 100.0) / 100.0);

        if (value <= getAddon().getMinPayAmount()) {
            sender.sendMessage(ChatColor.RED + "Minimum payment amount: $" + String.format("%.2f", getAddon().getMinPayAmount()));
            return;
        }

        if (sender instanceof Player) {
            final Player senderPlayer = (Player)sender;

            addon.getHandler().transferBalance(senderPlayer.getUniqueId(), receiver, amount, new SimplePromise() {
                @Override
                public void success() {}

                @Override
                public void failure(@Nonnull String reason) {
                    sender.sendMessage(ChatColor.RED + reason);
                }
            });

            return;
        }

        addon.getHandler().addToBalance(receiver, amount, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "$" + String.format("%.2f", amount) + " has been added to this user's balance");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("setbalance|setbal")
    @Description("Set the balance of a player")
    @Syntax("<player> <amount>")
    @CommandCompletion("@players")
    @CommandPermission("economy.balance.set")
    public void onSet(CommandSender sender, String receiver, double value) {
        if (value <= 0.0) {
            sender.sendMessage(ChatColor.RED + "Value must be greater than 0");
            return;
        }

        final double amount = Math.round((value * 100.0) / 100.0);

        addon.getHandler().setBalance(sender, receiver, amount, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Updated player's balance");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}