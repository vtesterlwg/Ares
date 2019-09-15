package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@AllArgsConstructor
public final class SpectateCommand extends BaseCommand {
    @Getter public final Arenas plugin;

    @CommandAlias("spectate|spec")
    @CommandCompletion("@players")
    public void onSpectate(Player spectator, String username) {
        plugin.getSpectateManager().getHandler().spectate(spectator, username, new SimplePromise() {
            @Override
            public void success() {
                spectator.sendMessage(ChatColor.GREEN + "You are now spectating " + username);
            }

            @Override
            public void failure(@Nonnull String reason) {
                spectator.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
