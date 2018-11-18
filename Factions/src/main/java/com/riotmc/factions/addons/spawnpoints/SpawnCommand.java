package com.riotmc.factions.addons.spawnpoints;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.factions.Factions;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class SpawnCommand extends BaseCommand {
    @Getter
    public final Factions plugin;

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
}