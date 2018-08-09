package com.playares.services.ranks;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.ranks.data.AresRank;
import com.playares.services.ranks.data.AresRankDAO;
import lombok.Getter;

public final class RankHandler {
    @Getter
    public RankService rankService;

    public RankHandler(RankService rankService) {
        this.rankService = rankService;
    }

    public void deleteRank(AresRank rank, SimplePromise promise) {
        new Scheduler(rankService.getOwner()).async(() -> {
            AresRankDAO.deleteRank(rankService.getOwner().getMongo(), rank);
            new Scheduler(rankService.getOwner()).sync(promise::success).run();
        }).run();
    }
}
