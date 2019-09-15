package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Values;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@AllArgsConstructor
@CommandAlias("spawn")
public final class SpawnCommand extends BaseCommand {
    @Getter public final Arenas plugin;


    @CommandAlias("spawn")
    @Description("Return to the spawn")
    public void onSpawn(Player player) {
        plugin.getSpawnManager().getHandler().teleport(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Returned to Spawn");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("spawn")
    @CommandPermission("arena.admin")
    @Description("Update the spawn location")
    public void onSpawnSet(Player player, @Values("set") String set) {
        plugin.getSpawnManager().getHandler().update(player);
    }
}
