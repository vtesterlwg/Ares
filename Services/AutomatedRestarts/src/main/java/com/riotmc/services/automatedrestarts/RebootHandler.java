package com.riotmc.services.automatedrestarts;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class RebootHandler {
    @Getter
    public AutomatedRestartService service;

    public RebootHandler(AutomatedRestartService service) {
        this.service = service;
    }

    public void cancel(CommandSender sender, SimplePromise promise) {
        if (!service.isInProgress()) {
            promise.failure("There is not a reboot in progress");
            return;
        }

        service.setInProgress(false);
        Bukkit.broadcastMessage(AutomatedRestartService.PREFIX + " Reboot has been cancelled");
        Logger.print(sender.getName() + " cancelled the current reboot process");
        promise.success();
    }

    public void schedule(CommandSender sender, String time, SimplePromise promise) {
        final long ms;

        try {
            ms = Time.parseTime(time);
        } catch (NumberFormatException ex) {
            promise.failure("Invalid time format");
            return;
        }

        if (service.isInProgress()) {
            service.setInProgress(false);
            Bukkit.broadcastMessage(AutomatedRestartService.PREFIX + " Reboot process has been cancelled");
        }

        service.setRebootCommenceTime(Time.now() + ms);
        Bukkit.broadcastMessage(AutomatedRestartService.PREFIX + " Reboot has been rescheduled to occur in " + ChatColor.YELLOW + Time.convertToRemaining(service.getTimeUntilReboot()));
        Logger.print(sender.getName() + " rescheduled the reboot to happen " + (ms / 1000) + " seconds from now");
        promise.success();
    }

    public void start(CommandSender sender, SimplePromise promise) {
        startCountdown(AutomatedRestartService.DEFAULT_REBOOT_TIME);
        Logger.print(sender.getName() + " manually started a reboot");
        promise.success();
    }

    public void start(CommandSender sender, String time, SimplePromise promise) {
        final long ms;

        try {
            ms = Time.parseTime(time);
        } catch (NumberFormatException ex) {
            promise.failure("Invalid time format");
            return;
        }

        startCountdown((int)(ms / 1000L));
        Logger.print(sender.getName() + " manually started a reboot");
        promise.success();
    }

    public void time(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "The server is expected to restart in " + ChatColor.BLUE + Time.convertToRemaining(service.getTimeUntilReboot()));
    }

    public void startCountdown(int seconds) {
        service.setRebootTime(Time.now() + (seconds * 1000));
        service.setInProgress(true);
        Bukkit.broadcastMessage(AutomatedRestartService.PREFIX + " " +
                ChatColor.RED + "The server will restart in " + ChatColor.YELLOW + Time.convertToRemaining(service.getTimeUntilReboot()));
    }
}
