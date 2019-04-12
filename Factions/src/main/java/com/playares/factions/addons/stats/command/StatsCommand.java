package com.playares.factions.addons.stats.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.stats.handler.StatsHandler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class StatsCommand extends BaseCommand {
    @Getter
    public final StatsHandler handler;

    public StatsCommand(StatsHandler handler) {
        this.handler = handler;
    }

    @CommandAlias("statistics|stat|stats")
    @Description("View your stats")
    @CommandCompletion("@players")
    public void onStats(Player player) {
        handler.getStats(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("statistics|stat|stats")
    @Description("View a player's stats")
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onStats(Player player, String name) {
        handler.getStats(player, name, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("leaderboard|lb")
    @Description("View the player leaderboards")
    @Syntax("[category]")
    public void onLeaderboard(Player player) {
        handler.getPlugin().getPlayerManager().getDisplayHandler().displayLeaderboard(player, "elo", new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("leaderboard|lb")
    @Description("View the player leaderboards")
    @Syntax("[category]")
    public void onLeaderboard(Player player, @Flags("rating|elo|e|kill|kills|k|death|deaths|d|minorevent|minorevents|minor|majorevent|majorevents|major|diamond|diamonds|emerald|emeralds|gold|redstone|lapis|lazuli|iron|coal|playtime|pt") String category) {
        handler.getPlugin().getPlayerManager().getDisplayHandler().displayLeaderboard(player, category, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}