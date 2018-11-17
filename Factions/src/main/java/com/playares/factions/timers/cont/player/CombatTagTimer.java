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
 * Represents a players combat-tag
 */
public final class CombatTagTimer extends PlayerTimer {
    @Getter public final Factions plugin;

    public CombatTagTimer(Factions plugin, UUID owner, int seconds) {
        super(owner, PlayerTimerType.COMBAT, seconds);
        this.plugin = plugin;
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null && profile.hasCombatShields()) {
            profile.hideAllCombatShields();
        }

        player.sendMessage(ChatColor.GREEN + "You are no longer combat-tagged");
    }
}