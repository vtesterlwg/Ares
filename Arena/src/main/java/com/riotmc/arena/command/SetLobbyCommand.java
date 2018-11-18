package com.riotmc.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.riotmc.arena.Arenas;
import com.riotmc.commons.bukkit.location.PLocatable;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class SetLobbyCommand extends BaseCommand {
    @Nonnull @Getter
    public final Arenas plugin;

    public SetLobbyCommand(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("setlobby|sl")
    @CommandPermission("arena.setlobby")
    @Description("Update the lobby location")
    public void onLobbyUpdate(Player player) {
        plugin.getPlayerHandler().setLobby(new PLocatable(player));
        player.sendMessage(ChatColor.GREEN + "Lobby location updated");
    }
}