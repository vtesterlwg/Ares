package com.playares.arena.queue;

import com.playares.arena.player.ArenaPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public final class SearchingPlayer {
    @Getter public final ArenaPlayer player;
    @Getter public final MatchmakingQueue.QueueType queueType;
    @Getter @Setter public RankedData rankedData;

    public boolean isRanked() {
        return rankedData != null;
    }

    public class RankedData {
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
}