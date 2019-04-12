package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("team|t|party|p")
public final class TeamCommand extends BaseCommand {
    @Getter public final Arenas plugin;

    public TeamCommand(Arenas plugin) {
        this.plugin = plugin;
    }

    @Subcommand("open")
    @Description("Allow anyone to join your team")
    public void onOpen(Player player) {
        plugin.getTeamManager().getHandler().open(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("close")
    @Description("Allow players to only join with an invitation")
    public void onClose(Player player) {
        plugin.getTeamManager().getHandler().close(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("invite|inv")
    @Description("Send a player an invitation to join your team")
    public void onInvite(Player player, String username) {
        plugin.getTeamManager().getHandler().invite(player, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("join|accept")
    @Description("Accept an invitation you have received to join a team")
    public void onJoin(Player player, String team) {
        plugin.getTeamManager().getHandler().accept(player, team, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("leave")
    @Description("Leave your team")
    public void onLeave(Player player) {
        plugin.getTeamManager().getHandler().leave(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.YELLOW + "You have left the team");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("kick|remove")
    @Description("Remove a player from your team")
    public void onKick(Player player, String username) {
        plugin.getTeamManager().getHandler().kick(player, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}