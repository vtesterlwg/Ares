package com.riotmc.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.playares.commons.base.promise.SimplePromise;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.player.ArenaPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class SpectateCommand extends BaseCommand {
    @Nonnull @Getter
    public final Arenas plugin;

    public SpectateCommand(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("spectate|spec")
    @Description("Spectate matches")
    @Syntax("<player>")
    public void onSpectate(Player player, String name) {
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final ArenaPlayer target = plugin.getPlayerManager().getPlayer(name);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        plugin.getSpectatorHandler().startSpectating(profile, target, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.YELLOW + "You are now spectating " + ChatColor.LIGHT_PURPLE + target.getUsername());
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}