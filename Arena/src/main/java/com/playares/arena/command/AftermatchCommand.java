package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

@CommandAlias("aftermatch|am")
public final class AftermatchCommand extends BaseCommand {
    @Nonnull @Getter
    public Arenas plugin;

    public AftermatchCommand(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @Subcommand("player|p")
    public void onPlayerLookup(Player player, String playerId, String matchId) {
        final UUID pid;
        final UUID mid;

        try {
            pid = UUID.fromString(playerId);
            mid = UUID.fromString(matchId);
        } catch (IllegalArgumentException ex) {
            player.sendMessage(ChatColor.RED + "Invalid player/match ID");
            return;
        }

        plugin.getMatchHandler().openPlayerReport(player, pid, mid, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("team|t")
    public void onTeamLookup(Player player, String teamId, String matchId) {
        final UUID tid;
        final UUID mid;

        try {
            tid = UUID.fromString(teamId);
            mid = UUID.fromString(matchId);
        } catch (IllegalArgumentException ex) {
            player.sendMessage(ChatColor.RED + "Invalid team/match ID");
            return;
        }

        plugin.getMatchHandler().openTeamReport(player, tid, mid, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
