package com.riotmc.services.deathban.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.services.deathban.DeathbanService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class ReviveCommand extends BaseCommand {
    @Getter public final DeathbanService service;

    public ReviveCommand(DeathbanService service) {
        this.service = service;
    }

    @CommandAlias("revive")
    @Description("Revive yourself")
    @Syntax("[player]")
    public void onRevive(Player player) {
        service.revive(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You are now revived");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("revive")
    @Description("Revive a player")
    @Syntax("[player]")
    public void onRevive(CommandSender sender, String username) {
        service.revive(sender, username, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been revived");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}