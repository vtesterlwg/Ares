package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("loadout")
public final class LoadoutCommand extends BaseCommand {
    @Getter
    public final Arenas plugin;

    public LoadoutCommand(Arenas plugin) {
        this.plugin = plugin;
    }

    @Subcommand("create")
    @CommandPermission("arena.loadout.create")
    @Syntax("/loadout create <name>")
    @Description("Create a new loadout")
    public void onCreate(Player player, String name) {
        plugin.getLoadoutHandler().createLoadout(player.getInventory().getContents(), player.getInventory().getArmorContents(), name, new SimplePromise() {
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

    @Subcommand("list")
    @CommandPermission("arena.loadout.list")
    @Syntax("/loadout list")
    @Description("List all loadouts")
    public void onList(Player player) {
        // TODO: Loadout Lists
    }
}