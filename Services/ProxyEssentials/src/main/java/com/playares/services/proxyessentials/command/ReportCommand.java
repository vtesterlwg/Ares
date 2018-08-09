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
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class ReportCommand extends BaseCommand {
    @Getter
    public ProxyEssentialsService service;

    @CommandAlias("report")
    @Description("Report a player")
    @Syntax("/report <player> <reason>")
    public void onReport(ProxiedPlayer player, String name, String message) {
        final ProxiedPlayer reported = service.getProxy().getProxy().getPlayer(name);

        if (reported == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        if (reported.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can not report yourself");
            return;
        }

        service.getReportHandler().create(player, reported, message, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Your report has been sent");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
