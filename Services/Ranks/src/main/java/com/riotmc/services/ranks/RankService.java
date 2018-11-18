package com.riotmc.services.ranks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.event.ProcessedChatEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.RiotService;
import com.riotmc.services.ranks.command.RankCommand;
import com.riotmc.services.ranks.data.RiotRank;
import com.riotmc.services.ranks.data.RiotRankDAO;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RankService implements RiotService, Listener {
    @Getter
    public final RiotPlugin owner;

    @Getter
    public final Set<RiotRank> ranks;

    @Getter
    public final RankHandler rankHandler;

    public RankService(RiotPlugin owner) {
        this.owner = owner;
        this.ranks = Sets.newConcurrentHashSet();
        this.rankHandler = new RankHandler(this);
    }

    public void start() {
        getOwner().getCommandManager().getCommandCompletions().registerAsyncCompletion("ranks", c -> {
            final List<String> rankNames = Lists.newArrayList();

            for (RiotRank rank : ranks) {
                if (rank.getName() == null) {
                    continue;
                }

                rankNames.add(rank.getName());
            }

            return ImmutableList.copyOf(rankNames);
        });

        registerCommand(new RankCommand(this));
        registerListener(this);

        this.ranks.addAll(RiotRankDAO.getRanks(getOwner().getMongo()));
        Logger.print("Loaded " + this.ranks.size() + " Ranks");
    }

    public void stop() {
        this.ranks.clear();
    }

    public String getName() {
        return "Ranks";
    }

    public RiotRank getRank(String name) {
        return ranks.stream().filter(rank -> rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<RiotRank> getSortedRanks() {
        final List<RiotRank> result = Lists.newArrayList(ranks.stream().filter(RiotRank::isSetup).collect(Collectors.toList()));

        result.sort(Comparator.comparingInt(RiotRank::getWeight));
        Collections.reverse(result);

        return result;
    }

    public List<RiotRank> getDefaultRanks() {
        return ranks.stream().filter(rank -> rank.isEveryone() && rank.isSetup()).collect(Collectors.toList());
    }

    public List<RiotRank> getStaffRanks() {
        return ranks.stream().filter(rank -> rank.isStaff() && rank.isSetup()).collect(Collectors.toList());
    }

    public List<RiotRank> getRanks(Player player) {
        return ranks.stream()
                .filter(RiotRank::isSetup)
                .filter(rank -> rank.isEveryone() ||
                (rank.getPermission() != null && player.hasPermission(rank.getPermission()))).collect(Collectors.toList());
    }

    public RiotRank getHighestRank(Player player) {
        final List<RiotRank> result = getRanks(player);

        if (result.isEmpty()) {
            return null;
        }

        result.sort(Comparator.comparingInt(RiotRank::getWeight));
        Collections.reverse(result);

        return result.get(0);
    }

    public String getDisplayName(Player player) {
        final RiotRank rank = getHighestRank(player);

        if (rank == null || rank.getPrefix() == null) {
            return player.getName();
        }

        return rank.getPrefix() + player.getName() + ChatColor.RESET;
    }

    public boolean isStaff(Player player) {
        for (RiotRank rank : getRanks(player)) {
            if (rank.isStaff()) {
                return true;
            }
        }

        return false;
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onProcessedChat(ProcessedChatEvent event) {
        event.setDisplayName(getDisplayName(event.getPlayer()));
    }
}
