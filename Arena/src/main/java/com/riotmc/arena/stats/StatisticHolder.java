package com.riotmc.arena.stats;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface StatisticHolder {
    @Nonnull
    UUID getUniqueId();

    int getHits();

    double getDamage();

    void addHit();

    void addDamage(double amount);
}
