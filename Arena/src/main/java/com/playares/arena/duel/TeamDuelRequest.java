package com.playares.arena.duel;

import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.team.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class TeamDuelRequest implements DuelRequest {
    @Getter public final Team requesting;
    @Getter public final Team requested;
    @Getter public final MatchmakingQueue.QueueType queueType;

    @Override
    public void accept() {

    }
}
