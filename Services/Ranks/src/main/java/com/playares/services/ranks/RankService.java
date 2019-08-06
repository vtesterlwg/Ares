package com.playares.services.ranks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.ranks.command.RankCommand;
import com.playares.services.ranks.data.Rank;
import com.playares.services.ranks.data.RankDAO;
import com.playares.services.ranks.listener.RankListener;
import lombok.Getter;
import me.lucko.luckperms.api.LuckPermsApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RankService implements AresService, Listener {
    @Getter public final AresPlugin owner;
    @Getter public final Set<Rank> ranks;
    @Getter public LuckPermsApi luckPerms;

    public RankService(AresPlugin owner) {
        this.owner = owner;
        this.ranks = Sets.newConcurrentHashSet();
    }

    @Override
    public String getName() {
        return "Ranks";
    }

    @Override
    public void start() {
        final RegisteredServiceProvider<LuckPermsApi> provider = Bukkit.getServicesManager().getRegistration(LuckPermsApi.class);

        if (provider != null) {
            luckPerms = provider.getProvider();
        }

        new Scheduler(getOwner()).async(() -> {
            ranks.addAll(RankDAO.getRanks(getOwner().getMongo()));

            new Scheduler(getOwner()).sync(() -> {
                Logger.print("Loaded " + ranks.size() + " ranks");

                getOwner().getCommandManager().getCommandCompletions().registerAsyncCompletion("ranks", c -> {
                    final List<String> names = Lists.newArrayList();

                    ranks.stream().filter(rank -> rank.getName() != null).forEach(rank -> names.add(rank.getName()));

                    return ImmutableList.copyOf(names);
                });
            }).run();
        }).run();

        registerListener(new RankListener(this));
        registerCommand(new RankCommand(this));
    }

    @Override
    public void stop() {
        ranks.clear();
    }

    public Rank getRankByName(String name) {
        return ranks.stream().filter(rank -> rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Rank getRankByDisplayName(String displayName) {
        return ranks.stream().filter(rank -> ChatColor.stripColor(rank.getDisplayName()).equalsIgnoreCase(displayName)).findFirst().orElse(null);
    }

    public ImmutableList<Rank> getRanksByWeight() {
        final List<Rank> result = Lists.newArrayList(ranks.stream().filter(Rank::isReady).collect(Collectors.toList()));

        result.sort(Comparator.comparingInt(Rank::getWeight));
        Collections.reverse(result);

        return ImmutableList.copyOf(result);
    }

    public ImmutableList<Rank> getRanksByAlphabetical() {
        final List<Rank> result = Lists.newArrayList(ranks.stream().filter(Rank::isReady).collect(Collectors.toList()));

        result.sort(Comparator.comparing(Rank::getName));

        return ImmutableList.copyOf(result);
    }

    public ImmutableList<Rank> getStaffRanks() {
        return ImmutableList.copyOf(ranks.stream().filter(rank -> rank.isStaff() && rank.isReady()).collect(Collectors.toList()));
    }

    public ImmutableList<Rank> getDefaultRanks() {
        return ImmutableList.copyOf(ranks.stream().filter(rank -> rank.isEveryone() && rank.isReady()).collect(Collectors.toList()));
    }

    public List<Rank> getRanks(Player player) {
        return ranks.stream()
                .filter(Rank::isReady)
                .filter(rank -> rank.isEveryone() || (rank.getPermission() != null && player.hasPermission(rank.getPermission())))
                .collect(Collectors.toList());
    }

    public Rank getHighestRank(Player player) {
        final List<Rank> result = getRanks(player);

        if (result.isEmpty()) {
            return null;
        }

        result.sort(Comparator.comparingInt(Rank::getWeight));
        Collections.reverse(result);

        return result.get(0);
    }

    public String formatName(Player player) {
        final Rank rank = getHighestRank(player);

        if (rank == null || rank.getPrefix() == null) {
            return player.getName();
        }

        return rank.getPrefix() + player.getName() + ChatColor.RESET;
    }

    public boolean isStaffMember(Player player) {
        for (Rank rank : getRanks(player)) {
            if (rank.isStaff()) {
                return true;
            }
        }

        return false;
    }
}