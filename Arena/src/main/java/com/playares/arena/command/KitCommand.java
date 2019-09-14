package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@AllArgsConstructor
@CommandAlias("arenakit|akit")
public final class KitCommand extends BaseCommand {
    @Getter public final Arenas plugin;

    @Subcommand("create")
    @Description("Create a new Arena kit")
    @CommandPermission("arena.kit.create")
    public void onCreate(Player player, String name) {
        plugin.getKitManager().getHandler().create(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Kit '" + name + "' has been created");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}