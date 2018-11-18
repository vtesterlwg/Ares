package com.riotmc.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.riotmc.services.essentials.EssentialsService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class BroadcastCommand extends BaseCommand {
    @Getter
    public final String prefix;

    @Getter
    public final String playerPrefixFormat;

    public BroadcastCommand(EssentialsService essentialsService) {
        prefix = ChatColor.translateAlternateColorCodes('&', essentialsService.getEssentialsConfig().getString("broadcasts.global-prefix"));
        playerPrefixFormat = ChatColor.translateAlternateColorCodes('&', essentialsService.getEssentialsConfig().getString("broadcasts.player-prefix-format"));
    }

    @CommandAlias("broadcast|b")
    @CommandPermission("essentials.broadcast")
    @Description("Broadcast a message")
    @Syntax("<message>")
    public void onBroadcast(String message) {
        Bukkit.broadcastMessage(prefix + message);
    }

    @CommandAlias("pbroadcast|pb")
    @CommandPermission("essentials.broadcast")
    @Description("Broadcast a message as a player")
    @Syntax("<message>")
    public void onBroadcast(Player player, String message) {
        Bukkit.broadcastMessage(playerPrefixFormat.replace("{player}", player.getName()) + message);
    }
}