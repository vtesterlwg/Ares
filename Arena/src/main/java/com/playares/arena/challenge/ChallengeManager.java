package com.playares.arena.challenge;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.challenge.cont.DuelChallenge;
import com.playares.arena.challenge.cont.TeamChallenge;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public final class ChallengeManager {
    @Nonnull @Getter
    public Arenas plugin;

    @Nonnull @Getter
    public final Set<Challenge> challenges;

    public ChallengeManager(@Nonnull Arenas plugin) {
        this.plugin = plugin;
        this.challenges = Sets.newConcurrentHashSet();
    }

    @Nullable
    public Challenge getChallenge(@Nonnull UUID uniqueId) {
        return challenges.stream().filter(challenge -> challenge.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public void clearPendingDuels(@Nonnull ArenaPlayer player) {
        challenges.stream()
                .filter(challenge -> challenge instanceof DuelChallenge)
                .filter(challenge -> ((DuelChallenge) challenge).getChallenger().equals(player))
                .forEach(challenges::remove);
    }

    public void clearPendingTeamfights(@Nonnull Team team) {
        challenges.stream()
                .filter(challenge -> challenge instanceof TeamChallenge)
                .filter(challenge -> ((TeamChallenge) challenge).getChallenger().equals(team))
                .forEach(challenges::remove);
    }

    public boolean hasPendingTeamfight(@Nonnull Team sender, @Nonnull Team receiver, @Nonnull Mode mode) {
        for (Challenge challenge : challenges) {
            if (!(challenge instanceof TeamChallenge)) {
                continue;
            }

            final TeamChallenge teamfight = (TeamChallenge)challenge;

            if (!teamfight.getChallenger().equals(sender)) {
                continue;
            }

            if (!teamfight.getChallenged().equals(receiver)) {
                continue;
            }

            if (!challenge.getMode().equals(mode)) {
                continue;
            }

            return true;
        }

        return false;
    }

    public boolean hasPendingDuel(@Nonnull ArenaPlayer sender, @Nonnull ArenaPlayer receiver, @Nonnull Mode mode) {
        for (Challenge challenge : challenges) {
            if (!(challenge instanceof DuelChallenge)) {
                continue;
            }

            final DuelChallenge duel = (DuelChallenge)challenge;

            if (!duel.getChallenger().equals(sender)) {
                continue;
            }

            if (!duel.getChallenged().equals(receiver)) {
                continue;
            }

            if (!challenge.getMode().equals(mode)) {
                continue;
            }

            return true;
        }

        return false;
    }
}
