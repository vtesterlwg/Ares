package com.riotmc.services.proxyessentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.services.proxyessentials.ProxyEssentialsService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class HelpopCommand extends BaseCommand {
    @Getter
    public final ProxyEssentialsService service;

    @CommandAlias("helpop|request")
    @Description("Request help from a staff member")
    @Syntax("/helpop <message>")
    public void onHelpop(ProxiedPlayer player, String request) {
        service.getRequestHandler().create(player, request, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Your request has been sent");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}