package com.playares.lobby.queue;

import com.playares.services.serversync.data.Server;
import com.playares.services.serversync.event.ServerSyncedEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public final class QueueDataListener implements Listener {
    @Getter public final QueueManager manager;

    @EventHandler
    public void onServerSyncComplete(ServerSyncedEvent event) {
        for (Server updated : event.getServers()) {
            final Server server = manager.getServerQueues().keySet().stream().filter(s -> s.getId() == updated.getId() && s.getType().equals(updated.getType())).findFirst().orElse(null);

            if (server != null) {
                server.setOnlineCount(updated.getOnlineCount());
                server.setStatus(updated.getStatus());
                server.setDescription(updated.getDescription());
                server.setDisplayName(updated.getDisplayName());
                continue;
            }

            manager.getServerQueues().put(updated, new ServerQueue(updated));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        for (ServerQueue queue : getManager().getServerQueues().values()) {
            queue.remove(player.getUniqueId());
        }
    }
}