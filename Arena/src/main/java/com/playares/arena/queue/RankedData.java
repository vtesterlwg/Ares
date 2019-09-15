package com.playares.arena.queue;

import lombok.Getter;
import lombok.Setter;

public final class RankedData {
    @Getter public final int rating;
    @Getter @Setter public int margin;

    public RankedData(int rating) {
        this.rating = rating;
        this.margin = 100;
    }

    public int getMinAcceptedRating() {
        return rating - margin;
    }

    public int getMaxAcceptedRating() {
        return rating + margin;
    }

    public boolean isAccepted(int rating) {
        return rating >= getMinAcceptedRating() && rating <= getMaxAcceptedRating();
    }
}
