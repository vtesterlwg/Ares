package com.playares.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.Factions;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@CommandAlias("timer")
@CommandPermission("factions.timer.edit")
public final class TimerCommand extends BaseCommand {
    @Getter public final Factions plugin;

    public TimerCommand(Factions plugin) {
        this.plugin = plugin;
    }

    @Subcommand("apply|give")
    @Description("Apply or modify a timer")
    @Syntax("<player> <timer> <time>")
    public void onApply(CommandSender sender, String username, String timer, String time) {
        plugin.getPlayerManager().getTimerHandler().apply(sender, username, timer, time, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Timer has been updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("remove")
    @Description("Remove an existing timer from a player")
    @Syntax("<player> <timer>")
    public void onRemove(CommandSender sender, String username, String timer) {
        plugin.getPlayerManager().getTimerHandler().remove(sender, username, timer, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Timer has been removed");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("list")
    @Description("List all valid Player Timer types")
    public void onList(CommandSender sender) {
        plugin.getPlayerManager().getTimerHandler().list(sender);
    }
}
