package com.playares.services.deathban.listener;

import com.playares.services.deathban.DeathbanService;
import com.playares.services.deathban.dao.DeathbanDAO;
import com.playares.services.deathban.data.Deathban;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

@AllArgsConstructor
public final class DeathbanListener implements Listener {
    @Getter public final DeathbanService service;

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (!service.getConfiguration().isDeathbanEnforced()) {
            return;
        }

        final UUID uniqueId = event.getUniqueId();
        final Deathban deathban = DeathbanDAO.getDeathban(service.getOwner().getMongo(), uniqueId);

        if (deathban != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, service.getDeathbanKickMessage(deathban));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
    }
}