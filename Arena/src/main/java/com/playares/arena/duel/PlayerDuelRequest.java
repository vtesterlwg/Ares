package com.playares.arena.duel;

import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class PlayerDuelRequest implements DuelRequest {
    @Getter public final ArenaPlayer requesting;
    @Getter public final ArenaPlayer requested;
    @Getter public final MatchmakingQueue.QueueType queueType;

    @Override
    public void accept() {
        // GET QUEUE BY QUEUE TYPE
        // BUILD PLAYERDUELSESSION
        // TELEPORT PLAYERS TO SPAWNPOINTS
        // GIVE KIT BOOK ITEMS
        // SET PLAYER STATUSES TO INGAME
    }
}
