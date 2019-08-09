package com.playares.factions.addons.loggers.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.loggers.LoggerAddon;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class LogoutCommand extends BaseCommand {
    @Getter public final LoggerAddon addon;

    public LogoutCommand(LoggerAddon addon) {
        this.addon = addon;
    }

    @CommandAlias("logout|safelog")
    public void onLogout(Player player) {
        addon.attemptSafeLogout(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.YELLOW + "You will be safely logged out of the server in " + ChatColor.AQUA + getAddon().getLogoutTimerDuration() + " seconds" + ChatColor.YELLOW + ". Moving or taking damage will cancel this timer");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}