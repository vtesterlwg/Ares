package com.riotmc.services.punishments.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.riotmc.services.punishments.PunishmentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class BanCommand extends BaseCommand {
    @Getter
    public final PunishmentService service;

    @CommandAlias("ban")
    @CommandPermission("punishments.ban")
    @CommandCompletion("@players")
    @Description("Ban a player forever")
    @Syntax("<player> <reason>")
    public void onBan(CommandSender sender, String name, String reason) {
        service.getPunishmentHandler().ban(sender, name, reason, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been banned");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("tempban")
    @CommandPermission("punishments.tempban")
    @CommandCompletion("@players")
    @Description("Ban a player for a set amount of time")
    @Syntax("<player> <time> <reason>")
    public void onTempban(CommandSender sender, String name, String time, String reason) {
        service.getPunishmentHandler().tempban(sender, name, time, reason, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been banned");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
