package com.playares.arena.arena;

import com.playares.arena.arena.data.Arena;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class ArenaHandler {
    @Getter public ArenaManager manager;

    public void teleport(Player player, String name, SimplePromise promise) {
        final Arena arena = getManager().getArena(name);

        if (arena == null) {
            promise.failure("Arena not found");
            return;
        }

        arena.teleportToSpectatorSpawnpoint(player);
        promise.success();
    }
}
