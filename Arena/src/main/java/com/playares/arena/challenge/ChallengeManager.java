package com.playares.arena.challenge;

import com.google.common.collect.Sets;
import com.playares.arena.Arenas;
import com.playares.arena.challenge.cont.DuelChallenge;
import com.playares.arena.challenge.cont.TeamChallenge;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

public final class ChallengeManager {
    @Getter
    public Arenas plugin;

    @Getter
    public final Set<Challenge> challenges;

    public ChallengeManager(Arenas plugin) {
        this.plugin = plugin;
        this.challenges = Sets.newConcurrentHashSet();
    }

    public Challenge getChallenge(UUID uniqueId) {
        return challenges.stream().filter(challenge -> challenge.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public boolean hasPendingTeamfight(Team sender, Team receiver, Mode mode) {
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

    public boolean hasPendingDuel(ArenaPlayer sender, ArenaPlayer receiver, Mode mode) {
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
