package com.playares.factions.addons.stats.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.factions.addons.stats.StatsAddon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("stats|stat")
@AllArgsConstructor
public final class StatsCommand extends BaseCommand {
    @Getter public final StatsAddon addon;

    @CommandAlias("stats|stat")
    public void onStats(Player player) {
        addon.getPlayerHandler().view(player, new FailablePromise<Menu>() {
            @Override
            public void success(@Nonnull Menu menu) {
                menu.open();
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("faction|f")
    @CommandCompletion("@playerfactions")
    public void onFaction(Player player, String name) {
        addon.getFactionHandler().view(player, name, new FailablePromise<Menu>() {
            @Override
            public void success(@Nonnull Menu menu) {
                menu.open();
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("player|p")
    @CommandCompletion("@players")
    public void onPlayer(Player player, String name) {
        addon.getPlayerHandler().view(player, name, new FailablePromise<Menu>() {
            @Override
            public void success(@Nonnull Menu menu) {
                menu.open();
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("server|s")
    public void onServer(Player player) {
        player.sendMessage("Coming soon!");
    }

    @HelpCommand
    @Description("How to use the Stats Command")
    public void onHelp(Player player, CommandHelp help) {
        help.showHelp();
    }
}
