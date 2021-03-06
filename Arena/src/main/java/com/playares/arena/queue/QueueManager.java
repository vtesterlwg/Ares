package com.playares.arena.queue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.kit.Kit;
import com.playares.arena.match.Match;
import com.playares.arena.match.RankedMatch;
import com.playares.arena.match.UnrankedMatch;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class QueueManager {
    @Getter public final Arenas plugin;
    @Getter public final QueueHandler handler;
    @Getter public final Set<MatchmakingQueue> matchmakingQueues;
    @Getter public final Set<SearchingPlayer> searchingPlayers;
    @Getter public final BukkitTask matchmakingTask;

    public QueueManager(Arenas plugin) {
        this.plugin = plugin;
        this.handler = new QueueHandler(this);
        this.matchmakingQueues = Sets.newHashSet();
        this.searchingPlayers = Sets.newConcurrentHashSet();

        this.matchmakingTask = new Scheduler(plugin).sync(() -> {
            if (searchingPlayers.isEmpty()) {
                return;
            }

            for (MatchmakingQueue queue : matchmakingQueues) {
                final MatchmakingQueue.QueueType type = queue.getQueueType();

                final Set<SearchingPlayer> searching = getSearchingByQueue(type);

                if (searching.isEmpty() || searching.size() < 2) {
                    continue;
                }

                final Set<SearchingPlayer> toRemove = Sets.newHashSet();

                for (SearchingPlayer search : searching) {
                    if (toRemove.contains(search)) {
                        continue;
                    }

                    Match match = null;
                    SearchingPlayer matchedPlayer = null;

                    for (SearchingPlayer otherPlayer : searching) {
                        if (toRemove.contains(otherPlayer)) {
                            continue;
                        }

                        if (otherPlayer.getPlayer().getUniqueId().equals(search.getPlayer().getUniqueId())) {
                            continue;
                        }

                        if (search.isRanked() && otherPlayer.isRanked() && search.getRankedData().isAccepted(otherPlayer.getRankedData().getRating())) {
                            final Arena arena = plugin.getArenaManager().obtainArena();

                            if (arena == null) {
                                continue;
                            }

                            match = new RankedMatch(plugin, queue, arena, otherPlayer.getPlayer(), search.getPlayer());
                            matchedPlayer = otherPlayer;
                            break;
                        }

                        if (!search.isRanked() && !otherPlayer.isRanked()) {
                            final Arena arena = plugin.getArenaManager().obtainArena();

                            if (arena == null) {
                                continue;
                            }

                            match = new UnrankedMatch(plugin, queue, arena, otherPlayer.getPlayer(), search.getPlayer());
                            matchedPlayer = otherPlayer;
                            break;
                        }
                    }

                    if (match == null) {
                        if (search.isRanked()) {
                            search.getRankedData().setMargin(search.getRankedData().getMargin() + 50);

                            search.getPlayer().getPlayer().sendMessage(ChatColor.YELLOW + "You are searching for a " + ChatColor.RED + "ranked" + ChatColor.GOLD + " " + search.getQueueType().getDisplayName() + ChatColor.YELLOW + " match " +
                                    ChatColor.GOLD + "(" + ChatColor.GREEN + search.getRankedData().getMinAcceptedRating() + ChatColor.YELLOW + "-" + ChatColor.GREEN + search.getRankedData().getMaxAcceptedRating() + ChatColor.GOLD + ")");
                        }

                        continue;
                    }

                    toRemove.add(search);
                    toRemove.add(matchedPlayer);

                    if (match instanceof UnrankedMatch) {
                        final UnrankedMatch unrankedMatch = (UnrankedMatch)match;

                        if (unrankedMatch.isRanked()) {
                            final RankedMatch rankedMatch = (RankedMatch)unrankedMatch;
                            rankedMatch.start();
                            plugin.getMatchManager().getMatches().add(rankedMatch);
                        } else {
                            unrankedMatch.start();
                            plugin.getMatchManager().getMatches().add(unrankedMatch);
                        }
                    }
                }

                if (toRemove.isEmpty()) {
                    return;
                }

                searchingPlayers.removeAll(toRemove);
            }
        }).repeat(5 * 20L, 5 * 20L).run();
    }

    public void load() {
        final YamlConfiguration config = getPlugin().getConfig("queues");

        if (config == null) {
            Logger.error("Failed to obtain queues.yml");
            return;
        }

        if (!matchmakingQueues.isEmpty()) {
            matchmakingQueues.clear();
            Logger.warn("Cleared matchmaking queues while reloading");
            return;
        }

        for (String typeName : config.getConfigurationSection("queues").getKeys(false)) {
            final MatchmakingQueue.QueueType type;
            final List<Kit> kits = Lists.newArrayList();

            try {
                type = MatchmakingQueue.QueueType.valueOf(typeName);
            } catch (IllegalArgumentException ex) {
                Logger.error("Invalid Queue Type: " + typeName + "... Skipping " + typeName);
                continue;
            }

            for (String kitName : config.getStringList("queues." + typeName)) {
                final Kit kit = plugin.getKitManager().getKit(kitName);

                if (kit != null) {
                    kits.add(kit);
                } else {
                    Logger.error("Invalid kit name: " + kitName + " for queue " + typeName + ", skipping!");
                }
            }

            Logger.print("Loaded " + kits.size() + " Kits for Queue Type: " + type.getDisplayName());

            final MatchmakingQueue queue = new MatchmakingQueue(type, kits);
            matchmakingQueues.add(queue);

            Logger.print("Loaded Queue: " + queue.getQueueType().getDisplayName());
        }
    }

    public MatchmakingQueue getQueueByType(MatchmakingQueue.QueueType type) {
        return matchmakingQueues.stream().filter(queue -> queue.getQueueType().equals(type)).findFirst().orElse(null);
    }

    public SearchingPlayer getCurrentSearch(Player player) {
        return searchingPlayers.stream().filter(searchingPlayer -> searchingPlayer.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    public ImmutableSet<SearchingPlayer> getSearchingByQueue(MatchmakingQueue.QueueType type) {
        return ImmutableSet.copyOf(searchingPlayers.stream().filter(search -> search.getQueueType().equals(type)).collect(Collectors.toSet()));
    }
}