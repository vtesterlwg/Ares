package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@AllArgsConstructor
@CommandAlias("duel")
public final class DuelCommand extends BaseCommand {
    @Getter public final Arenas plugin;

    @CommandAlias("duel")
    @Description("Send a duel request to a named player")
    @Syntax("[player]")
    public void onDuel(Player player, String username) {
        plugin.getDuelManager().getHandler().openDuelMenu(player, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("accept")
    @Description("Accept a duel request")
    @Syntax("[player]")
    public void onAccept(Player player, String username) {
        plugin.getDuelManager().getHandler().acceptDuel(player, username, new SimplePromise() {
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
