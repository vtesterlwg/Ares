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
public final class HelpopCommand extends BaseCommand {
    @Getter public final ProxyEssentialsService service;

    @CommandAlias("helpop|request")
    @Description("Request help from a staff member")
    @Syntax("<message>")
    public void onHelpop(ProxiedPlayer player, String request) {
        service.getRequestHandler().create(player, request, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(new ComponentBuilder("Your request has been sent").color(ChatColor.GREEN).create());
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}