package com.playares.arena.spawn;

import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.PLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class SpawnHandler {
    @Getter public final SpawnManager manager;

    public void update(Player player) {
        manager.setSpawn(new PLocatable(player));
        manager.save();

        player.sendMessage(ChatColor.GREEN + "Spawn location updated");
    }

    public void teleport(Player player) {
        player.teleport(manager.getSpawn().getBukkit());
    }

    public void teleport(Player player, SimplePromise promise) {
        final ArenaPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player);

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        if (!profile.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure("This command can only be used while in the lobby");
            return;
        }

        teleport(player);
    }
}
