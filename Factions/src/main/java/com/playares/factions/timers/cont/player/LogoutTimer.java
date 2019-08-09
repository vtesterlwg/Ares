package com.playares.factions.timers.cont.player;

import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
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

        factionPlayer.setSafelogging(true);
        player.kickPlayer(ChatColor.GREEN + "You have been safely logged off from the server");
        Logger.print(player.getName() + " safe-logged");
    }
}