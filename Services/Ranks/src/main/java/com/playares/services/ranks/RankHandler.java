package com.playares.services.ranks;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.ranks.data.AresRank;
import com.playares.services.ranks.data.AresRankDAO;
import lombok.Getter;
import org.bukkit.ChatColor;

public final class RankHandler {
    @Getter public RankService rankService;

    public RankHandler(RankService rankService) {
        this.rankService = rankService;
    }

    public void createRank(String name, SimplePromise promise) {
        final AresRank existing = rankService.getRank(name);

        if (existing != null) {
            promise.failure("Rank name is already in use");
            return;
        }

        final AresRank rank = new AresRank(name);
        rankService.getRanks().add(rank);
        promise.success();
    }

    public void deleteRank(AresRank rank, SimplePromise promise) {
        new Scheduler(rankService.getOwner()).async(() -> {
            rankService.getRanks().remove(rank);
            AresRankDAO.deleteRank(rankService.getOwner().getMongo(), rank);
            new Scheduler(rankService.getOwner()).sync(promise::success).run();
        }).run();
    }

    public void setDisplay(String rankName, String displayName, SimplePromise promise) {
        final AresRank rank = rankService.getRank(rankName);
        final String converted = ChatColor.translateAlternateColorCodes('&', displayName);

        if (rank == null) {
            promise.failure("Rank not found");
            return;
        }

        rank.setDisplayName(converted);

        if (rank.isSetup()) {
            new Scheduler(rankService.getOwner()).async(() -> {
                AresRankDAO.insertRank(rankService.getOwner().getMongo(), rank);
                new Scheduler(rankService.getOwner()).sync(promise::success).run();
            }).run();

            return;
        }

        promise.success();
    }

    public void setPrefix(String rankName, String prefix, SimplePromise promise) {
        final AresRank rank = rankService.getRank(rankName);
        final String converted = ChatColor.translateAlternateColorCodes('&', prefix);

        if (rank == null) {
            promise.failure("Rank not found");
            return;
        }

        rank.setPrefix(converted);

        if (rank.isSetup()) {
            new Scheduler(rankService.getOwner()).async(() -> {
                AresRankDAO.insertRank(rankService.getOwner().getMongo(), rank);
                new Scheduler(rankService.getOwner()).sync(promise::success).run();
            }).run();

            return;
        }

        promise.success();
    }

    public void setPermission(String rankName, String permission, SimplePromise promise) {
        final AresRank rank = rankService.getRank(rankName);

        if (rank == null) {
            promise.failure("Rank not found");
            return;
        }

        rank.setPermission(permission);

        if (rank.isSetup()) {
            new Scheduler(rankService.getOwner()).async(() -> {
                AresRankDAO.insertRank(rankService.getOwner().getMongo(), rank);
                new Scheduler(rankService.getOwner()).sync(promise::success).run();
            }).run();

            return;
        }

        promise.success();
    }

    public void setWeight(String rankName, int weight, SimplePromise promise) {
        final AresRank rank = rankService.getRank(rankName);

        if (rank == null) {
            promise.failure("Rank not found");
            return;
        }

        rank.setWeight(weight);

        if (rank.isSetup()) {
            new Scheduler(rankService.getOwner()).async(() -> {
                AresRankDAO.insertRank(rankService.getOwner().getMongo(), rank);
                new Scheduler(rankService.getOwner()).sync(promise::success).run();
            }).run();

            return;
        }

        promise.success();
    }

    public void setStaff(String rankName, boolean staff, SimplePromise promise) {
        final AresRank rank = rankService.getRank(rankName);

        if (rank == null) {
            promise.failure("Rank not found");
            return;
        }

        rank.setStaff(staff);

        if (rank.isSetup()) {
            new Scheduler(rankService.getOwner()).async(() -> {
                AresRankDAO.insertRank(rankService.getOwner().getMongo(), rank);
                new Scheduler(rankService.getOwner()).sync(promise::success).run();
            }).run();

            return;
        }

        promise.success();
    }

    public void setEveryone(String rankName, boolean everyone, SimplePromise promise) {
        final AresRank rank = rankService.getRank(rankName);

        if (rank == null) {
            promise.failure("Rank not found");
            return;
        }

        rank.setEveryone(everyone);

        if (rank.isSetup()) {
            new Scheduler(rankService.getOwner()).async(() -> {
                AresRankDAO.insertRank(rankService.getOwner().getMongo(), rank);
                new Scheduler(rankService.getOwner()).sync(promise::success).run();
            }).run();

            return;
        }

        promise.success();
    }
}