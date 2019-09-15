package com.playares.arena.duel;

import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.match.TeamMatch;
import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.team.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public final class TeamDuelRequest implements DuelRequest {
    @Getter public final Arenas plugin;
    @Getter public final Team requesting;
    @Getter public final Team requested;
    @Getter public final MatchmakingQueue.QueueType queueType;

    @Override
    public void accept() {
        final Arena arena = plugin.getArenaManager().obtainArena();

        if (arena == null) {
            requesting.sendMessage(ChatColor.RED + "Failed to obtain an arena");
            requested.sendMessage(ChatColor.RED + "Failed to obtain an arena");
            return;
        }

        final MatchmakingQueue queue = plugin.getQueueManager().getQueueByType(queueType);
        final TeamMatch match = new TeamMatch(plugin, queue, arena, requesting, requested);

        plugin.getMatchManager().getMatches().add(match);
        match.start();
    }
}
