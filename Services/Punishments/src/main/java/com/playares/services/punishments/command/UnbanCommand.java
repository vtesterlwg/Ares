package com.playares.services.punishments.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.punishments.PunishmentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class UnbanCommand extends BaseCommand {
    @Getter
    public final PunishmentService service;

    @CommandAlias("unban|pardon")
    @CommandPermission("punishments.unban")
    @Description("Unban a player")
    @Syntax("<player>")
    public void onUnban(CommandSender sender, String name) {
        Bukkit.broadcastMessage("Command ran!");

        service.getPunishmentHandler().unban(sender, name, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been unbanned");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}