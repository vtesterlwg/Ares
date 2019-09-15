package com.playares.arena.duel;

import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.match.UnrankedMatch;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public final class PlayerDuelRequest implements DuelRequest {
    @Getter public final Arenas plugin;
    @Getter public final ArenaPlayer requesting;
    @Getter public final ArenaPlayer requested;
    @Getter public final MatchmakingQueue.QueueType queueType;

    @Override
    public void accept() {
        final Arena arena = plugin.getArenaManager().obtainArena();

        if (arena == null) {
            requesting.getPlayer().sendMessage(ChatColor.RED + "Failed to obtain an arena");
            requested.getPlayer().sendMessage(ChatColor.RED + "Failed to obtain an arena");
            return;
        }

        final MatchmakingQueue queue = plugin.getQueueManager().getQueueByType(queueType);
        final UnrankedMatch match = new UnrankedMatch(plugin, queue, arena, requesting, requested);

        plugin.getMatchManager().getMatches().add(match);

        match.start();
    }
}
