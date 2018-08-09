package com.playares.arena.challenge.cont;

import com.playares.arena.challenge.Challenge;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import lombok.Getter;

import java.util.UUID;

public final class DuelChallenge implements Challenge {
    @Getter
    public final UUID uniqueId;

    @Getter
    public final ArenaPlayer challenger;

    @Getter
    public final ArenaPlayer challenged;

    @Getter
    public final Mode mode;

    public DuelChallenge(ArenaPlayer challenger, ArenaPlayer challenged, Mode mode) {
        this.uniqueId = UUID.randomUUID();
        this.challenger = challenger;
        this.challenged = challenged;
        this.mode = mode;
    }
}