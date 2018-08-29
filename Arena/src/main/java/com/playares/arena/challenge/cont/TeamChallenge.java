package com.playares.arena.challenge.cont;

import com.playares.arena.challenge.Challenge;
import com.playares.arena.mode.Mode;
import com.playares.arena.team.Team;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class TeamChallenge implements Challenge {
    @Nonnull @Getter
    public final UUID uniqueId;

    @Nonnull @Getter
    public final Team challenger;

    @Nonnull @Getter
    public final Team challenged;

    @Nonnull @Getter
    public final Mode mode;

    public TeamChallenge(@Nonnull Team challenger, @Nonnull Team challenged, @Nonnull Mode mode) {
        this.uniqueId = UUID.randomUUID();
        this.challenger = challenger;
        this.challenged = challenged;
        this.mode = mode;
    }
}