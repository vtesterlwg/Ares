package com.playares.factions.timers.cont.player;

import com.playares.factions.Factions;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a players PvP Protection timer
 */
public final class ProtectionTimer extends PlayerTimer {
    @Getter public final Factions plugin;

    public ProtectionTimer(Factions plugin, UUID owner, int seconds) {
        super(owner, PlayerTimerType.PROTECTION, seconds);
        this.plugin = plugin;
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null && profile.hasProtectionShields()) {
            profile.hideAllProtectionShields();
        }

        player.sendMessage(ChatColor.YELLOW + "Your PvP Protection has expired");
    }
}
