package com.playares.lobby.queue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.commons.base.util.Time;
import com.playares.services.ranks.data.Rank;
import com.playares.services.serversync.data.Server;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public final class ServerQueue {
    @Getter final Server server;
    private final List<QueuedPlayer> queue;

    public ServerQueue(Server server) {
        this.server = server;
        this.queue = Lists.newArrayList();
    }

    public void add(UUID uniqueId, Rank rank) {
        final int weight = (rank != null) ? rank.getWeight() : 0;
        queue.add(new QueuedPlayer(uniqueId, weight));
    }

    public void remove(UUID uniqueId) {
        final QueuedPlayer queuePlayer = getPlayer(uniqueId);

        if (queuePlayer != null) {
            queue.remove(queuePlayer);
        }
    }

    public int getPosition(UUID uniqueId) {
        if (getPlayer(uniqueId) == null) {
            return -1;
        }

        int pos = 1;

        for (QueuedPlayer queuePlayer : getQueue()) {
            if (queuePlayer.getUniqueId().equals(uniqueId)) {
                break;
            }

            pos += 1;
        }

        return pos;
    }

    public ImmutableList<QueuedPlayer> getQueue() {
        queue.sort((o1, o2) -> {
            if (o1.getWeight() == o2.getWeight()) {
                if (o1.getJoinTime() == o2.getJoinTime()) {
                    return 0;
                }

                if (o1.getJoinTime() >= o2.getJoinTime()) {
                    return 1;
                }

                return -1;
            }

            return o1.getWeight() - o2.getWeight();
        });

        return ImmutableList.copyOf(queue);
    }

    public QueuedPlayer getPlayer(UUID uniqueId) {
        return queue.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public final class QueuedPlayer {
        @Getter public final UUID uniqueId;
        @Getter public final long joinTime;
        @Getter @Setter public int weight;

        QueuedPlayer(UUID uniqueId, int weight) {
            this.uniqueId = uniqueId;
            this.joinTime = Time.now();
            this.weight = weight;
        }
    }
}