package com.playares.arena.match;

import com.playares.arena.aftermatch.PlayerReport;
import com.playares.arena.arena.Arena;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface Match {
    UUID getUniqueId();

    long getStartTimestamp();

    MatchStatus getStatus();

    Mode getMode();

    Arena getArena();

    void setArena(Arena arena);

    void setStatus(MatchStatus status);

    void setStartTimestamp(long timestamp);

    Set<ArenaPlayer> getSpectators();

    Set<PlayerReport> getPlayerReports();

    default PlayerReport getPlayerReport(ArenaPlayer player) {
        return getPlayerReports().stream().filter(report -> report.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    default PlayerReport getPlayerReport(UUID uniqueId) {
        return getPlayerReports().stream().filter(report -> report.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    default void sendMessage(Collection<ArenaPlayer> viewers, String message) {
        viewers.forEach(viewer -> {
            if (viewer.getPlayer() != null) {
                viewer.getPlayer().sendMessage(message);
            }
        });
    }

    default void sendMessage(Collection<ArenaPlayer> viewers, BaseComponent[] message) {
        viewers.forEach(viewer -> {
            if (viewer.getPlayer() != null) {
                viewer.getPlayer().sendMessage(message);
            }
        });
    }

    default void sendTitle(Collection<ArenaPlayer> viewers, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        viewers.forEach(viewer -> {
            if (viewer.getPlayer() != null) {
                viewer.getPlayer().sendTitle(title, subtitle, fadeIn, duration, fadeOut);
            }
        });
    }
}