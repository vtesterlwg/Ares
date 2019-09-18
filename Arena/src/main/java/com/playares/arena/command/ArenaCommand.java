package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

// TODO: Arena delete command
@AllArgsConstructor
@CommandAlias("arena")
public final class ArenaCommand extends BaseCommand {
    @Getter public final Arenas plugin;

    @Subcommand("create")
    public void onCreate(Player player, String name, String displayName) {
        plugin.getArenaManager().getBuilderManager().getHandler().create(player, name, displayName, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You have entered the Arena Builder");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("set")
    public void onSet(Player player) {
        plugin.getArenaManager().getBuilderManager().getHandler().set(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("list")
    public void onList(Player player) {
        player.sendMessage("Coming soon!");
    }

    @Subcommand("teleport|tp")
    public void onTeleport(Player player, String arena) {
        plugin.getArenaManager().getHandler().teleport(player, arena, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Teleported to the arena");
            }

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
