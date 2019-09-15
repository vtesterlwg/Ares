package com.playares.arena.queue;

import com.playares.arena.player.ArenaPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public final class SearchingPlayer {
    @Getter public final ArenaPlayer player;
    @Getter public final MatchmakingQueue.QueueType queueType;
    @Getter @Setter public RankedData rankedData;

    public boolean isRanked() {
        return rankedData != null;
    }
}