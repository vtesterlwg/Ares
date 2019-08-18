package com.playares.services.proxyessentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.proxyessentials.ProxyEssentialsService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class ReportCommand extends BaseCommand {
    @Getter public ProxyEssentialsService service;

    @CommandAlias("report")
    @Description("Report a player")
    @Syntax("<player> <reason>")
    public void onReport(ProxiedPlayer player, String name, String message) {
        final ProxiedPlayer reported = service.getProxy().getProxy().getPlayer(name);

        if (reported == null) {
            player.sendMessage(new ComponentBuilder("Player not found").color(ChatColor.RED).create());
            return;
        }

        if (reported.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(new ComponentBuilder("You can not report yourself").color(ChatColor.RED).create());
            return;
        }

        service.getReportHandler().create(player, reported, message, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(new ComponentBuilder("Your report has been sent").color(ChatColor.GREEN).create());
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
