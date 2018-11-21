package com.riotmc.services.punishments.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.services.punishments.PunishmentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class BlacklistCommand extends BaseCommand {
    @Getter
    public final PunishmentService service;

    @CommandAlias("blacklist")
    @CommandPermission("punishments.blacklist")
    @CommandCompletion("@players")
    @Description("Blacklist a player from the network")
    @Syntax("<player> <reason>")
    public void onBlacklist(CommandSender sender, String name, String reason) {
        service.getPunishmentHandler().blacklist(sender, name, reason, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been blacklisted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}