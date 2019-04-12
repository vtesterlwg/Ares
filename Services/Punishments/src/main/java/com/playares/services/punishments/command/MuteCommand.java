package com.playares.services.punishments.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.punishments.PunishmentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class MuteCommand extends BaseCommand {
    @Getter
    public final PunishmentService service;

    @CommandAlias("mute")
    @CommandPermission("punishments.mute")
    @CommandCompletion("@players")
    @Description("Mute a player forever")
    @Syntax("<player> <reason>")
    public void onMute(CommandSender sender, String name, String reason) {
        service.getPunishmentHandler().mute(sender, name, reason, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been muted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("tempmute")
    @CommandPermission("punishments.tempmute")
    @CommandCompletion("@players")
    @Description("Mute a player for a set amount of time")
    @Syntax("<player> <time> <reason>")
    public void onTempmute(CommandSender sender, String name, String time, String reason) {
        service.getPunishmentHandler().tempmute(sender, name, time, reason, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been muted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}