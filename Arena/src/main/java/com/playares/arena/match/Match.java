package com.playares.arena.match;

import com.playares.arena.aftermatch.PlayerReport;
import com.playares.arena.arena.Arena;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import net.md_5.bungee.api.chat.BaseComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface Match {
    @Nonnull
    UUID getUniqueId();

    long getStartTimestamp();

    @Nonnull
    MatchStatus getStatus();

    @Nonnull
    Mode getMode();

    @Nullable
    Arena getArena();

    void setArena(@Nonnull Arena arena);

    void setStatus(@Nonnull MatchStatus status);

    void setStartTimestamp(long timestamp);

    @Nonnull
    Set<ArenaPlayer> getSpectators();

    @Nonnull
    Set<PlayerReport> getPlayerReports();

    @Nullable
    default PlayerReport getPlayerReport(@Nonnull ArenaPlayer player) {
        return getPlayerReports().stream().filter(report -> report.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    @Nullable
    default PlayerReport getPlayerReport(@Nonnull UUID uniqueId) {
        return getPlayerReports().stream().filter(report -> report.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    default void sendMessage(@Nonnull Collection<ArenaPlayer> viewers, @Nonnull String message) {
        viewers.forEach(viewer -> {
            if (viewer.getPlayer() != null) {
                viewer.getPlayer().sendMessage(message);
            }
        });
    }

    default void sendMessage(@Nonnull Collection<ArenaPlayer> viewers, @Nonnull BaseComponent[] message) {
        viewers.forEach(viewer -> {
            if (viewer.getPlayer() != null) {
                viewer.getPlayer().sendMessage(message);
            }
        });
    }

    default void sendTitle(@Nonnull Collection<ArenaPlayer> viewers,
                           @Nonnull String title,
                           @Nonnull String subtitle,
                           int fadeIn,
                           int duration,
                           int fadeOut) {

        viewers.forEach(viewer -> {
            if (viewer.getPlayer() != null) {
                viewer.getPlayer().sendTitle(title, subtitle, fadeIn, duration, fadeOut);
            }
        });

    }
}