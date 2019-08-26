package com.playares.factions.timers.cont.player;

import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.services.serversync.ServerSyncService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a players logout timer
 */
public final class LogoutTimer extends PlayerTimer {
    @Getter public final Factions plugin;
    @Getter public final FactionPlayer factionPlayer;

    public LogoutTimer(Factions plugin, UUID owner, int seconds, FactionPlayer factionPlayer) {
        super(owner, PlayerTimerType.LOGOUT, seconds);
        this.plugin = plugin;
        this.factionPlayer = factionPlayer;
    }

    @Override
    public void onFinish() {
        final Player player = factionPlayer.getPlayer();

        if (player == null) {
            return;
        }

        final ServerSyncService serverSyncService = (ServerSyncService)getPlugin().getService(ServerSyncService.class);

        factionPlayer.setSafelogging(true);

        if (serverSyncService != null) {
            player.sendMessage(ChatColor.GREEN + "You have been safely logged off from the server");
            serverSyncService.sendToLobby(player);
        }

        Logger.print(player.getName() + " safe-logged");
    }
}