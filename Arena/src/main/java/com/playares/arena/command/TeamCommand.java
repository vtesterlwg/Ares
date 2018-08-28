package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("team|t")
public final class TeamCommand extends BaseCommand {
    @Getter
    public final Arenas plugin;

    public TeamCommand(Arenas plugin) {
        this.plugin = plugin;
    }

    @Subcommand("open")
    @Description("Open your team for others to join")
    public void onOpen(Player player) {
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        plugin.getTeamHandler().openTeam(profile, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Team opened");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("close")
    @Description("Limit access for your team to invite only")
    public void onClose(Player player) {
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        plugin.getTeamHandler().closeTeam(profile, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Team closed");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("invite|inv")
    @CommandCompletion("@players")
    public void onInvite(Player player, String name) {
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final ArenaPlayer invited = plugin.getPlayerManager().getPlayer(name);

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        if (invited == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        plugin.getTeamHandler().invitePlayer(profile, invited, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("join")
    @CommandCompletion("@players")
    public void onJoin(Player player, String name) {
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final ArenaPlayer target = plugin.getPlayerManager().getPlayer(name);

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        plugin.getTeamHandler().joinTeam(profile, target, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("kick")
    @CommandCompletion("@players")
    public void onKick(Player player, String name) {
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final ArenaPlayer target = plugin.getPlayerManager().getPlayer(name);

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        plugin.getTeamHandler().kickFromTeam(profile, target, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Player has been kicked");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
