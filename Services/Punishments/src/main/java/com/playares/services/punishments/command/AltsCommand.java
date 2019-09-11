package com.playares.services.punishments.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.punishments.PunishmentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class AltsCommand extends BaseCommand {
    @Getter public final PunishmentService service;

    @CommandAlias("alts|alt")
    @Description("Lookup alts connected to the provided username")
    @CommandPermission("punishments.alts")
    @CommandCompletion("@players")
    public void onLookup(CommandSender sender, String username) {
        service.getAltsHandler().lookup(sender, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
