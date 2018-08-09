package com.playares.arena.challenge.cont;

import com.playares.arena.challenge.Challenge;
import com.playares.arena.mode.Mode;
import com.playares.arena.team.Team;
import lombok.Getter;

import java.util.UUID;

public final class TeamChallenge implements Challenge {
    @Getter
    public final UUID uniqueId;

    @Getter
    public final Team challenger;

    @Getter
    public final Team challenged;

    @Getter
    public final Mode mode;

    public TeamChallenge(Team challenger, Team challenged, Mode mode) {
        this.uniqueId = UUID.randomUUID();
        this.challenger = challenger;
        this.challenged = challenged;
        this.mode = mode;
    }
}