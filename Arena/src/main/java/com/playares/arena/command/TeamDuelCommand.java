package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@AllArgsConstructor
@CommandAlias("teamduel|td")
public final class TeamDuelCommand extends BaseCommand {
    @Getter public final Arenas plugin;

    @Subcommand("accept")
    @Syntax("[player]")
    public void onAccept(Player player, String username) {
        plugin.getDuelManager().getHandler().acceptTeam(player, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @HelpCommand
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}
