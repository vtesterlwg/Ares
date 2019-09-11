package com.playares.factions.addons.boosts.data;

import com.playares.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;

public final class ActiveBoost {
    @Getter public final Boost boost;
    @Getter public final String username;
    @Getter @Setter public long expire;

    public ActiveBoost(Boost boost) {
        this.boost = boost;
        this.username = "Anonymous";
        this.expire = Time.now() + (boost.getDuration() * 1000);
    }

    public ActiveBoost(Boost boost, String username, int duration) {
        this.boost = boost;
        this.username = username;
        this.expire = Time.now() + (duration * 1000);
    }

    public boolean isExpired() {
        return expire <= Time.now();
    }
}