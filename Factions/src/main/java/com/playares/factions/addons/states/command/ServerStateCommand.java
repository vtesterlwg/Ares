package com.playares.factions.addons.states.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.factions.addons.states.ServerStateAddon;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("serverstate|ss")
public final class ServerStateCommand extends BaseCommand {
    @Getter public final ServerStateAddon addon;

    public ServerStateCommand(ServerStateAddon addon) {
        this.addon = addon;
    }

    @Subcommand("current")
    @Description("View the current state of the server")
    public void onCurrent(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Current Server State" + ChatColor.YELLOW + ": " + addon.getCurrentState().getDisplayName());
    }

    @Subcommand("set")
    @Description("Set the state of the server")
    @CommandPermission("factions.serverstates.set")
    public void onSet(CommandSender sender, @Values("sotw|normal|eotw") String state) {

    }
}
