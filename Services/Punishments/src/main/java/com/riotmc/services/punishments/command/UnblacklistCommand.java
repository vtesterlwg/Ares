package com.riotmc.services.punishments.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.services.punishments.PunishmentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class UnblacklistCommand extends BaseCommand {
    @Getter
    public final PunishmentService service;

    @CommandAlias("unblacklist")
    @CommandPermission("punishments.unblacklist")
    @Description("Unblacklist a player")
    @Syntax("<player>")
    public void onUnblacklist(CommandSender sender, String name) {
        service.getPunishmentHandler().unblacklist(sender, name, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been unblacklisted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
