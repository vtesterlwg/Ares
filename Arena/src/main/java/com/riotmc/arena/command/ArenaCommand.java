package com.riotmc.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.common.base.Joiner;
import com.riotmc.arena.Arenas;
import com.riotmc.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("arena")
public final class ArenaCommand extends BaseCommand {
    @Nonnull @Getter
    public final Arenas plugin;

    public ArenaCommand(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @Subcommand("create")
    @Syntax("<name>")
    @CommandPermission("arena.create")
    public void onCreate(Player player, String name) {
        plugin.getArenaHandler().createArena(name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Arena created" + ChatColor.WHITE + ": " + name);
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setspawn")
    @Syntax("<arena> <name>")
    @CommandCompletion("@arenalist")
    @CommandPermission("arena.create")
    public void onSetSpawn(Player player, String arena, @Flags("a|b") String spawnId) {
        plugin.getArenaHandler().setArenaSpawn(player, arena, spawnId, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Updated Spawnpoint " + spawnId.toUpperCase());
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setauthors")
    @Syntax("<arena> <authorA, authorB, authorC>")
    @CommandPermission("arena.create")
    public void onSetAuthors(Player player, String arena, String authors) {
        plugin.getArenaHandler().setAuthors(player, arena, authors, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Updated Authors");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("list")
    @CommandPermission("arena.list")
    public void onList(Player player) {
        player.sendMessage(ChatColor.GOLD + "Arenas" + ChatColor.YELLOW + ": " +
                ChatColor.WHITE + Joiner.on(", ").join(plugin.getArenaManager().getArenaList()));
    }

    @Subcommand("delete")
    @CommandPermission("arena.delete")
    @CommandCompletion("@arenalist")
    public void onDelete(Player player, String name) {
        plugin.getArenaHandler().deleteArena(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Arena Deleted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}