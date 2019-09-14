package com.playares.arena.timer.cont;

import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.timer.PlayerTimer;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class MatchEndingTimer extends PlayerTimer {
    @Getter public final Arenas plugin;

    public MatchEndingTimer(Arenas plugin, UUID owner, int seconds) {
        super(owner, PlayerTimerType.MATCH_ENDING, seconds);
        this.plugin = plugin;
    }

    @Override
    public void onFinish() {
        new Scheduler(plugin).sync(() -> {
            final Player player = Bukkit.getPlayer(owner);
            final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(owner);

            if (player == null || profile == null) {
                return;
            }

            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            profile.setStatus(ArenaPlayer.PlayerStatus.LOBBY);
            profile.getTimers().clear();

            plugin.getPlayerManager().getHandler().giveItems(profile);

            // TODO: Teleport back to lobby
        }).run();
    }
}
