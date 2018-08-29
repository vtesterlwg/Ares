package com.playares.arena.match.cont;

import com.google.common.collect.*;
import com.playares.arena.aftermatch.PlayerReport;
import com.playares.arena.arena.Arena;
import com.playares.arena.match.Match;
import com.playares.arena.match.MatchStatus;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class DuelMatch implements Match {
    @Nonnull @Getter
    public final UUID uniqueId;

    @Getter @Setter
    public long startTimestamp;

    @Nonnull @Getter @Setter
    public MatchStatus status;

    @Nonnull @Getter
    public final Mode mode;

    @Nullable @Getter @Setter
    public Arena arena;

    @Nonnull @Getter
    public Set<ArenaPlayer> opponents;

    @Nonnull @Getter
    public Set<ArenaPlayer> spectators;

    @Nonnull @Getter
    public final Set<PlayerReport> playerReports;

    public DuelMatch(@Nonnull ArenaPlayer playerA, @Nonnull ArenaPlayer playerB, @Nonnull Mode mode) {
        this.uniqueId = UUID.randomUUID();
        this.status = MatchStatus.COUNTDOWN;
        this.opponents = ImmutableSet.of(playerA, playerB);
        this.mode = mode;
        this.spectators = Sets.newConcurrentHashSet();
        this.playerReports = Sets.newConcurrentHashSet();
    }

    @Nullable
    public ArenaPlayer getPlayer(UUID uniqueId) {
        return opponents.stream().filter(player -> player.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    @Nonnull
    public ImmutableCollection<ArenaPlayer> getViewers() {
        final List<ArenaPlayer> result = Lists.newArrayList();
        result.addAll(opponents);
        result.addAll(spectators);
        return ImmutableList.copyOf(result);
    }

    @Nullable
    public ArenaPlayer getWinner() {
        final List<ArenaPlayer> alive = Lists.newArrayList();

        for (ArenaPlayer opponent : opponents) {
            if (opponent.getStatus().equals(PlayerStatus.INGAME)) {
                alive.add(opponent);
            }
        }

        if (alive.size() != 1) {
            return null;
        }

        return alive.get(0);
    }

    @Nullable
    public ArenaPlayer getLoser(@Nonnull ArenaPlayer winner) {
        for (ArenaPlayer opponent : opponents) {
            if (!opponent.equals(winner)) {
                return opponent;
            }
        }

        return null;
    }
}