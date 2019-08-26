package com.playares.lobby.queue;

import com.playares.services.serversync.data.Server;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class QueueHandler {
    @Getter public final QueueManager manager;

    void processQueues() {
        for (Server server : getManager().getServerQueues().keySet()) {
            final ServerQueue queue = getManager().getServerQueues().get(server);

            if (queue.getQueue().isEmpty()) {
                continue;
            }

            final ServerQueue.QueuedPlayer processed = queue.getQueue().get(0);
            final Player player = Bukkit.getPlayer(processed.getUniqueId());

            if (player == null) {
                queue.remove(processed.getUniqueId());
                continue;
            }

            if (server.status.equals(Server.Status.OFFLINE)) {
                continue;
            }

            if (server.status.equals(Server.Status.WHITELISTED) && !player.hasPermission("lobby.queue.bypasswhitelist")) {
                continue;
            }

            if (server.getOnlineCount() >= server.getMaxPlayers()) {
                continue;
            }

            if (server.isPremiumRequired() && (!player.hasPermission("lobby.premium") && !player.hasPermission("lobby.staff"))) {
                continue;
            }

            queue.remove(processed.getUniqueId());
            server.send(player);
        }
    }
}
