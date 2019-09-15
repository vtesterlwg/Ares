package com.playares.arena.player;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.report.PlayerReport;
import com.playares.arena.timer.PlayerTimer;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ArenaPlayer {
    @Getter public final UUID uniqueId;
    @Getter public final String username;
    @Getter @Setter public PlayerStatus status;
    @Getter @Setter public PlayerReport activeReport;
    @Getter @Setter public RankedData rankedData;
    @Getter public final Set<PlayerTimer> timers;

    public ArenaPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.status = PlayerStatus.LOBBY;
        this.activeReport = null;
        this.timers = Sets.newConcurrentHashSet();
        this.rankedData = new RankedData(player.getUniqueId());
    }

    public ArenaPlayer(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.status = PlayerStatus.LOBBY;
        this.activeReport = null;
        this.timers = Sets.newConcurrentHashSet();
        this.rankedData = new RankedData(uniqueId);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public boolean hasTimer(PlayerTimer.PlayerTimerType type) {
        return timers.stream().anyMatch(timer -> timer.getType().equals(type));
    }

    public PlayerTimer getTimer(PlayerTimer.PlayerTimerType type) {
        return timers.stream().filter(timer -> timer.getType().equals(type)).findFirst().orElse(null);
    }

    public void addTimer(PlayerTimer timer) {
        if (hasTimer(timer.getType())) {
            final PlayerTimer existing = getTimer(timer.getType());
            existing.setExpire(timer.getExpire());
            return;
        }

        timers.add(timer);
    }

    public void removeTimer(PlayerTimer.PlayerTimerType type) {
        if (!hasTimer(type)) {
            return;
        }

        final PlayerTimer timer = getTimer(type);

        timers.remove(timer);
    }

    public void update() {
        final List<String> hudElements = Lists.newArrayList();

        if (!timers.isEmpty()) {
            timers.stream().filter(timer -> !timer.isExpired() && timer.getType().isRender()).forEach(timer ->
                    hudElements.add(timer.getType().getDisplayName() + " " + ChatColor.RED + (timer.getType().isDecimal() ?
                            Time.convertToDecimal(timer.getRemaining()) : Time.convertToHHMMSS(timer.getRemaining()))));
        }

        if (!hudElements.isEmpty()) {
            getPlayer().sendActionBar(Joiner.on(ChatColor.RESET + " " + ChatColor.RESET + " ").join(hudElements));
        }

        timers.stream().filter(timer -> timer.isExpired() && !timer.isFrozen()).forEach(timer -> {
            timer.onFinish();
            timers.remove(timer);
        });
    }

    public enum PlayerStatus {
        LOBBY, INGAME, INGAME_DEAD, SPECTATING
    }

    public class RankedData implements MongoDocument<RankedData> {
        @Getter public UUID ownerId;
        @Getter public final Map<MatchmakingQueue.QueueType, Integer> ratings;
        @Getter @Setter public boolean loaded;

        RankedData(UUID ownerId) {
            this.ownerId = ownerId;
            this.ratings = Maps.newHashMap();
            this.loaded = false;
        }

        public int getRating(MatchmakingQueue.QueueType type) {
            return ratings.getOrDefault(type, 1000);
        }

        public void setRating(MatchmakingQueue.QueueType type, int rating) {
            ratings.put(type, rating);
        }

        @Override
        public RankedData fromDocument(Document document) {
            this.ownerId = (UUID)document.get("owner");

            for (String queueTypeName : document.keySet()) {
                final MatchmakingQueue.QueueType type;

                try {
                    type = MatchmakingQueue.QueueType.valueOf(queueTypeName);
                } catch (IllegalArgumentException ex) {
                    continue;
                }

                final int value = document.getInteger(queueTypeName);

                ratings.put(type, value);
            }

            loaded = true;

            return this;
        }

        @Override
        public Document toDocument() {
            final Document document = new Document();

            document.append("owner", ownerId);

            for (MatchmakingQueue.QueueType type : MatchmakingQueue.QueueType.values()) {
                final int value = ratings.getOrDefault(type, 1000);
                document.append(type.name(), value);
            }

            return document;
        }
    }
}