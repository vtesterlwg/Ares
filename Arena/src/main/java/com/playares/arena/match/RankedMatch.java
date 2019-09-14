package com.playares.arena.match;

import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.queue.MatchmakingQueue;

public final class RankedMatch extends Match {
    public RankedMatch(Arenas plugin, MatchmakingQueue queue, Arena arena, boolean ranked) {
        super(plugin, queue, arena, ranked);
    }
}
