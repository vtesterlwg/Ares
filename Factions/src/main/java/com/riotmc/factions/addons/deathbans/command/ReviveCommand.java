package com.riotmc.factions.addons.deathbans.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.factions.addons.deathbans.DeathbanAddon;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Syntax;

public final class ReviveCommand extends BaseCommand {
    @Getter public final DeathbanAddon addon;

    public ReviveCommand(DeathbanAddon addon) {
        this.addon = addon;
    }

    @CommandAlias("revive")
    @Syntax("<name>")
    @Description("Revive a player")
    public void onRevive(CommandSender sender, String username) {
        addon.getDeathbanManager().getHandler().revive(sender, username, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + username + " has been revived");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
