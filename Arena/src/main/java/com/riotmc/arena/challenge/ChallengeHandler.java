package com.riotmc.arena.challenge;

import com.google.common.base.Joiner;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.arena.Arena;
import com.riotmc.arena.challenge.cont.DuelChallenge;
import com.riotmc.arena.challenge.cont.TeamChallenge;
import com.riotmc.arena.match.cont.DuelMatch;
import com.riotmc.arena.match.cont.TeamMatch;
import com.riotmc.arena.mode.Mode;
import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.arena.player.PlayerStatus;
import com.riotmc.arena.team.Team;
import com.riotmc.arena.team.TeamStatus;
import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.util.Scheduler;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import javax.annotation.Nonnull;

public final class ChallengeHandler {
    @Nonnull @Getter
    public final Arenas plugin;

    public ChallengeHandler(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    public void sendChallenge(@Nonnull ArenaPlayer challenger, @Nonnull ArenaPlayer challenged, @Nonnull Mode mode, @Nonnull SimplePromise promise) {
        if (challenged.getPlayer() == null) {
            promise.failure("Player is not online");
            return;
        }

        if (challenger.getTeam() != null || challenger.getMatch() != null || !challenger.getStatus().equals(PlayerStatus.LOBBY)) {
            promise.failure("You must be in the lobby to perform this action");
            return;
        }

        if (challenged.getTeam() != null || challenged.getMatch() != null || !challenged.getStatus().equals(PlayerStatus.LOBBY)) {
            promise.failure("This player is no longer in the lobby");
            return;
        }

        if (plugin.getChallengeManager().hasPendingDuel(challenger, challenged, mode)) {
            promise.failure("Please wait a moment before trying to send this player another duel request");
            return;
        }

        final DuelChallenge challenge = new DuelChallenge(challenger, challenged, mode);
        final String challengerName = challenger.getUsername();

        challenged.getPlayer().sendMessage(new ComponentBuilder(challengerName)
                .color(ChatColor.AQUA)
                .append(" has challenged you to a ")
                .color(ChatColor.GOLD)
                .append(mode.getName())
                .color(ChatColor.AQUA)
                .append(" duel!")
                .color(ChatColor.GOLD)
                .append(" Click here to accept!")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + challenge.getUniqueId().toString()))
                .create());

        plugin.getChallengeManager().getChallenges().add(challenge);

        new Scheduler(plugin).sync(() -> plugin.getChallengeManager().getChallenges().remove(challenge)).delay(30 * 20L).run();

        promise.success();
    }

    public void sendChallenge(@Nonnull Team challenger, @Nonnull Team challenged, @Nonnull Mode mode, @Nonnull SimplePromise promise) {
        if (!challenger.getStatus().equals(TeamStatus.LOBBY)) {
            promise.failure("You must be in the lobby to perform this action");
            return;
        }

        if (!challenged.getStatus().equals(TeamStatus.LOBBY)) {
            promise.failure("This player is no longer in the lobby");
            return;
        }

        if (plugin.getChallengeManager().hasPendingTeamfight(challenger, challenged, mode)) {
            promise.failure("Please wait a moment before trying to send this player another duel request");
            return;
        }

        final TeamChallenge challenge = new TeamChallenge(challenger, challenged, mode);
        final ArenaPlayer challengedLeader = challenged.getLeader();
        final String challengerName = challenger.getLeader().getUsername();

        if (challengedLeader.getPlayer() == null) {
            promise.failure("Player is not online");
            return;
        }

        challengedLeader.getPlayer().sendMessage(
                new ComponentBuilder(challengerName)
                .color(ChatColor.AQUA)
                .append(" (" + challenger.getMembers().size() + ")")
                .color(ChatColor.YELLOW)
                .append(" has challenged your team to a ")
                .color(ChatColor.GOLD)
                .append(mode.getName())
                .color(ChatColor.AQUA)
                .append(" duel!")
                .color(ChatColor.GOLD)
                .append(" Click here to accept!")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + challenge.getUniqueId().toString()))
                .create()
        );

        plugin.getChallengeManager().getChallenges().add(challenge);

        new Scheduler(plugin).sync(() -> plugin.getChallengeManager().getChallenges().remove(challenge)).delay(30 * 20L).run();

        promise.success();
    }

    public void acceptChallenge(@Nonnull ArenaPlayer playerA, @Nonnull ArenaPlayer playerB, @Nonnull Mode mode) {
        if (playerA.getPlayer() == null || playerB.getPlayer() == null) {
            return;
        }

        if (playerA.getTeam() != null || playerA.getMatch() != null || !playerA.getStatus().equals(PlayerStatus.LOBBY)) {
            return;
        }

        if (playerB.getTeam() != null || playerB.getMatch() != null || !playerB.getStatus().equals(PlayerStatus.LOBBY)) {
            return;
        }

        plugin.getChallengeManager().clearPendingDuels(playerA);
        plugin.getChallengeManager().clearPendingDuels(playerB);

        final DuelMatch match = new DuelMatch(playerA, playerB, mode);

        plugin.getArenaHandler().startArena(match, new FailablePromise<Arena>() {
            @Override
            public void success(@Nonnull Arena arena) {
                final String nowPlaying;

                if (arena.getAuthors().isEmpty()) {
                    nowPlaying = ChatColor.YELLOW + "You are now playing " + ChatColor.AQUA + arena.getName();
                } else {
                    nowPlaying = ChatColor.YELLOW + "You are now playing " + ChatColor.AQUA + arena.getName() +
                            ChatColor.YELLOW + " by " + ChatColor.AQUA + Joiner.on(ChatColor.YELLOW + ", " + ChatColor.AQUA).join(arena.getAuthors());
                }

                mode.giveBooks(playerA.getPlayer());
                mode.giveBooks(playerB.getPlayer());

                playerA.getPlayer().sendMessage(nowPlaying);
                playerB.getPlayer().sendMessage(nowPlaying);

                plugin.getMatchManager().getMatches().add(match);
            }

            @Override
            public void failure(@Nonnull String reason) {
                playerA.getPlayer().sendMessage(ChatColor.RED + reason);
                playerB.getPlayer().sendMessage(ChatColor.RED + reason);
            }
        });
    }

    public void acceptChallenge(@Nonnull Team teamA, @Nonnull Team teamB, @Nonnull Mode mode) {
        if (!teamA.getStatus().equals(TeamStatus.LOBBY) || !teamB.getStatus().equals(TeamStatus.LOBBY)) {
            return;
        }

        plugin.getChallengeManager().clearPendingTeamfights(teamA);
        plugin.getChallengeManager().clearPendingTeamfights(teamB);

        final TeamMatch match = new TeamMatch(teamA, teamB, mode);

        plugin.getArenaHandler().startArena(match, new FailablePromise<Arena>() {
            @Override
            public void success(@Nonnull Arena arena) {
                final String nowPlaying;

                if (arena.getAuthors().isEmpty()) {
                    nowPlaying = ChatColor.YELLOW + "You are now playing " + ChatColor.AQUA + arena.getName();
                } else {
                    nowPlaying = ChatColor.YELLOW + "You are now playing " + ChatColor.AQUA + arena.getName() +
                            ChatColor.YELLOW + " by " + ChatColor.AQUA + Joiner.on(ChatColor.YELLOW + ", " + ChatColor.AQUA).join(arena.getAuthors());
                }

                teamA.getMembers().stream().filter(member -> member.getPlayer() != null).forEach(member -> mode.giveBooks(member.getPlayer()));
                teamB.getMembers().stream().filter(member -> member.getPlayer() != null).forEach(member -> mode.giveBooks(member.getPlayer()));

                teamA.sendMessage(nowPlaying);
                teamB.sendMessage(nowPlaying);

                plugin.getMatchManager().getMatches().add(match);
            }

            @Override
            public void failure(@Nonnull String reason) {
                teamA.sendMessage(ChatColor.RED + reason);
                teamB.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}
