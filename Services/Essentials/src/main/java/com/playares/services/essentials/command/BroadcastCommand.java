package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.services.essentials.EssentialsService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class BroadcastCommand extends BaseCommand {
    @Getter public EssentialsService service;

    @CommandAlias("broadcast|b")
    @CommandPermission("essentials.broadcast")
    @Description("Broadcast a message")
    @Syntax("<message>")
    public void onBroadcast(String message) {
        Bukkit.broadcastMessage(getService().getEssentialsConfig().getGlobalBroadcastPrefix() + message);
    }

    @CommandAlias("pbroadcast|pb")
    @CommandPermission("essentials.broadcast")
    @Description("Broadcast a message as a player")
    @Syntax("<message>")
    public void onBroadcast(Player player, String message) {
        Bukkit.broadcastMessage(getService().getEssentialsConfig().getPlayerBroadcastPrefix().replace("{player}", player.getName()) + message);
    }

    @HelpCommand
    @Description("View a list of Broadcast Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}
