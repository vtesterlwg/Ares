package com.riotmc.factions.addons.deathbans.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.factions.addons.deathbans.DeathbanAddon;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

@CommandAlias("deathban|db")
public final class DeathbanCommand extends BaseCommand {
    @Getter public final DeathbanAddon addon;

    public DeathbanCommand(DeathbanAddon addon) {
        this.addon = addon;
    }

    @CommandPermission("factions.deathban.create")
    @Description("Deathban a player")
    @Syntax("<name> <time> [-p]")
    public void onDeathban(CommandSender sender, String username, String time, @Optional String permanent) {

    }

    @Subcommand("clear")
    @Description("Clear all deathbans")
    public void onClear(CommandSender sender) {
        addon.getDeathbanManager().getHandler().clear(new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Deathbans have been cleared");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
