package com.riotmc.services.proxyessentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.riotmc.services.proxyessentials.ProxyEssentialsService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@AllArgsConstructor
public final class StaffChatCommand extends BaseCommand {
    @Getter
    public ProxyEssentialsService service;

    @CommandAlias("staffchat|sc")
    @CommandPermission("proxyessentials.staffchat")
    @Description("Toggle Staff Chat")
    @Syntax("/staffchat")
    public void onStaffChatToggle(ProxiedPlayer player) {
        if (service.getStaffChatManager().isInStaffChat(player)) {
            service.getStaffChatManager().getStaffChat().remove(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You are no longer speaking in Staff Chat");
            return;
        }

        service.getStaffChatManager().getStaffChat().add(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "You are now speaking in Staff Chat");
    }
}