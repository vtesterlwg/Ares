package com.playares.arena.queue;

import com.playares.arena.kit.Kit;
import com.playares.arena.player.ArenaPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class UnrankedQueue implements MatchmakingQueue {
    @Getter public final ArenaPlayer player;
    @Getter public final String name;
    @Getter public final String displayName;
    @Getter public final Kit kit;
}
