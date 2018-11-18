package com.riotmc.arena.challenge.cont;

import com.riotmc.arena.challenge.Challenge;
import com.riotmc.arena.mode.Mode;
import com.riotmc.arena.player.ArenaPlayer;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class DuelChallenge implements Challenge {
    @Nonnull @Getter
    public final UUID uniqueId;

    @Nonnull @Getter
    public final ArenaPlayer challenger;

    @Nonnull @Getter
    public final ArenaPlayer challenged;

    @Nonnull @Getter
    public final Mode mode;

    public DuelChallenge(@Nonnull ArenaPlayer challenger, @Nonnull ArenaPlayer challenged, @Nonnull Mode mode) {
        this.uniqueId = UUID.randomUUID();
        this.challenger = challenger;
        this.challenged = challenged;
        this.mode = mode;
    }
}