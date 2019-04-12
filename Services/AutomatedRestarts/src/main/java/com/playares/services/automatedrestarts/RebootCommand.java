package com.playares.services.automatedrestarts;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@CommandAlias("reboot|restart")
public final class RebootCommand extends BaseCommand {
    @Getter public final AutomatedRestartService service;

    public RebootCommand(AutomatedRestartService service) {
        this.service = service;
    }

    @Subcommand("time|t")
    @Description("View the remaining time before the next reboot occurs")
    public void onTime(CommandSender sender) {
        service.getHandler().time(sender);
    }

    @Subcommand("cancel")
    @Description("Cancel an active reboot attempt")
    @CommandPermission("autoreboot.cancel")
    public void onCancel(CommandSender sender) {
        service.getHandler().cancel(sender, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Reboot cancelled");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("start")
    @Description("Manually start the reboot process with a default time")
    @CommandPermission("autoreboot.start")
    public void onStart(CommandSender sender) {
        service.getHandler().start(sender, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Reboot process has started");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("start")
    @Description("Manually start the reboot process with a specified time")
    @Syntax("[time]")
    @CommandPermission("autoreboot.start")
    public void onStart(CommandSender sender, String time) {
        service.getHandler().start(sender, time, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Reboot process has started");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("schedule|set")
    @Description("Change the schedule of the next reboot")
    @Syntax("[time]")
    @CommandPermission("autoreboot.schedule")
    public void onSchedule(CommandSender sender, String time) {
        service.getHandler().schedule(sender, time, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Reboot has been rescheduled");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
