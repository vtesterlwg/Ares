package com.playares.factions.addons.states.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.states.ServerStateAddon;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@CommandAlias("serverstate|ss")
public final class ServerStateCommand extends BaseCommand {
    @Getter public final ServerStateAddon addon;

    public ServerStateCommand(ServerStateAddon addon) {
        this.addon = addon;
    }

    @Subcommand("current")
    @Description("View the current state of the server")
    public void onCurrent(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Current State of the Server" + ChatColor.YELLOW + ": " + addon.getCurrentState().getDisplayName());
    }

    @Subcommand("set")
    @Description("Set the state of the server")
    @CommandPermission("factions.serverstate.set")
    public void onSet(CommandSender sender, String stateName) {
        addon.performUpdate(sender, stateName, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "State of the server has been updated to " + addon.currentState.getDisplayName());
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("list")
    @Description("View a list of possible server states")
    @CommandPermission("factions.serverstate.list")
    public void onList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Valid States" + ChatColor.YELLOW + ": SOTW, NORMAL, EP1, EP2");
    }

    @HelpCommand
    @Description("View a list of Server State Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}
