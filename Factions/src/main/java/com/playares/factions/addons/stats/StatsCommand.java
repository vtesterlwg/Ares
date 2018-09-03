package com.playares.factions.addons.stats;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.playares.commons.base.promise.SimplePromise;
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
}