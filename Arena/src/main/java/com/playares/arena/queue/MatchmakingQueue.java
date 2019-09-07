package com.playares.arena.queue;

import com.playares.arena.kit.Kit;
import com.playares.arena.player.ArenaPlayer;

public interface MatchmakingQueue {
    ArenaPlayer getPlayer();

    String getName();

    String getDisplayName();

    Kit getKit();
}
