package com.playares.lobby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.playares.lobby.Lobby;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
public final class ReloadCommand extends BaseCommand {
    @Getter public final Lobby plugin;

    @CommandAlias("lobbyreload")
    @CommandPermission("lobby.reload")
    @Description("Reload the lobby")
    public void onReload(CommandSender sender) {
        plugin.getSpawnManager().load();
        plugin.reloadServices();
        plugin.getNameplateManager().build();
        sender.sendMessage(ChatColor.GREEN + "Lobby has been reloaded");
    }
}
