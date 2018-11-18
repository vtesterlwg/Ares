package com.riotmc.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.riotmc.arena.Arenas;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("loadout")
public final class LoadoutCommand extends BaseCommand {
    @Nonnull @Getter
    public final Arenas plugin;

    public LoadoutCommand(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @Subcommand("create")
    @CommandPermission("arena.loadout.create")
    @Syntax("<name>")
    @Description("Create a new loadout")
    public void onCreate(Player player, String name) {
        plugin.getLoadoutHandler().createStandardLoadout(player.getInventory().getContents(), player.getInventory().getArmorContents(), name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Loadout created");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("create")
    @CommandPermission("arena.loadout.create")
    @Syntax("<name> <type>")
    @Description("Create a new class-based loadout")
    public void onCreate(Player player, String name, String typeName) {
        plugin.getLoadoutHandler().createClassLoadout(player.getInventory().getContents(), player.getInventory().getArmorContents(), name, typeName, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Loadout created");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("delete")
    @CommandPermission("arena.loadout.delete")
    @Syntax("/loadout delete <name>")
    @Description("Delete a loadout")
    public void onDelete(Player player, String name) {
        plugin.getLoadoutHandler().deleteLoadout(name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Loadout deleted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}