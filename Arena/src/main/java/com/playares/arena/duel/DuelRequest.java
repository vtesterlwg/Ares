package com.playares.arena.duel;

import com.playares.arena.Arenas;
import com.playares.arena.queue.MatchmakingQueue;

public interface DuelRequest {
    Arenas getPlugin();

    MatchmakingQueue.QueueType getQueueType();

    void accept();
}
