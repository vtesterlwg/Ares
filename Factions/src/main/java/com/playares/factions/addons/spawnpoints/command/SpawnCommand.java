package com.playares.factions.addons.spawnpoints.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.Factions;
import com.playares.factions.addons.spawnpoints.SpawnpointAddon;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class SpawnCommand extends BaseCommand {
    @Getter public final Factions plugin;

    public SpawnCommand(Factions plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("spawn")
    @CommandPermission("factions.spawn")
    @Description("Teleport to the Overworld Spawn")
    public void onSpawn(Player player) {
        final SpawnpointAddon addon = (SpawnpointAddon)plugin.getAddonManager().getAddon(SpawnpointAddon.class);

        if (addon == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain Spawnpoint Addon");
            return;
        }

        addon.getManager().getHandler().teleport(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Teleported to spawn");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("spawn")
    @CommandPermission("factions.spawn")
    @Description("Teleport to the Overworld Spawn")
    @Syntax("[type]")
    public void onSpawn(Player player, String name) {
        final SpawnpointAddon addon = (SpawnpointAddon)plugin.getAddonManager().getAddon(SpawnpointAddon.class);

        if (addon == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain Spawnpoint Addon");
            return;
        }

        addon.getManager().getHandler().teleport(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Teleported to spawn");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("setspawn")
    @CommandPermission("factions.setspawn")
    @Description("Set a spawnpoint location to your current location")
    @Syntax("[type]")
    public void onSetSpawn(Player player, String name) {
        final SpawnpointAddon addon = (SpawnpointAddon)plugin.getAddonManager().getAddon(SpawnpointAddon.class);

        if (addon == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain Spawnpoint Addon");
            return;
        }

        addon.getManager().getHandler().setSpawn(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Spawn point has been updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @HelpCommand
    @Description("View a list of Spawn Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}