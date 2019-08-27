package com.playares.lobby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import com.playares.lobby.Lobby;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class SpawnCommand extends BaseCommand {
    @Getter public final Lobby plugin;

    @CommandAlias("spawn")
    @Description("Return to spawn")
    public void onSpawn(Player player) {
        getPlugin().getSpawnManager().teleport(player);
        player.sendMessage(ChatColor.GREEN + "Teleported to Spawn");
    }

    @CommandAlias("setspawn")
    @Description("Set the spawn")
    @CommandPermission("lobby.setspawn")
    public void onSetSpawn(Player player) {
        getPlugin().getSpawnManager().getHandler().setSpawn(player);
    }

    @HelpCommand
    @Description("View Spawn Commands")
    public void onHelp(Player player, CommandHelp help) {
        help.showHelp();
    }
}
