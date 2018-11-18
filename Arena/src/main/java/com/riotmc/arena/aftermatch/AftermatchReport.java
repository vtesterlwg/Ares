package com.riotmc.arena.aftermatch;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface AftermatchReport {
    @Nonnull
    UUID getUniqueId();

    @Nonnull
    UUID getMatchId();

    int getHits();

    double getDamage();
}
