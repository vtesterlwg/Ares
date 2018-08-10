package com.playares.arena.challenge;

import com.google.common.base.Joiner;
import com.playares.arena.Arenas;
import com.playares.arena.arena.Arena;
import com.playares.arena.challenge.cont.DuelChallenge;
import com.playares.arena.challenge.cont.TeamChallenge;
import com.playares.arena.match.cont.DuelMatch;
import com.playares.arena.match.cont.TeamMatch;
import com.playares.arena.mode.Mode;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import com.playares.arena.team.Team;
import com.playares.arena.team.TeamStatus;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import javax.annotation.Nonnull;

public final class ChallengeHandler {
    @Getter
    public final Arenas plugin;

    public ChallengeHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void sendChallenge(ArenaPlayer challenger, ArenaPlayer challenged, Mode mode, SimplePromise promise) {
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

    public void sendChallenge(Team challenger, Team challenged, Mode mode, SimplePromise promise) {
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

    public void acceptChallenge(ArenaPlayer playerA, ArenaPlayer playerB, Mode mode) {
        if (playerA.getTeam() != null || playerA.getMatch() != null || !playerA.getStatus().equals(PlayerStatus.LOBBY)) {
            return;
        }

        if (playerB.getTeam() != null || playerB.getMatch() != null || !playerB.getStatus().equals(PlayerStatus.LOBBY)) {
            return;
        }

        final DuelMatch match = new DuelMatch(playerA, playerB, mode);

        plugin.getArenaHandler().startArena(match, new FailablePromise<Arena>() {
            @Override
            public void success(Arena arena) {
                final String nowPlaying = ChatColor.YELLOW + "You are now playing " + ChatColor.AQUA + arena.getName() +
                        ChatColor.YELLOW + " by " + ChatColor.AQUA + Joiner.on(ChatColor.YELLOW + ", " + ChatColor.AQUA).join(arena.getAuthors());

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

    public void acceptChallenge(Team teamA, Team teamB, Mode mode) {
        if (!teamA.getStatus().equals(TeamStatus.LOBBY) || !teamB.getStatus().equals(TeamStatus.LOBBY)) {
            return;
        }

        final TeamMatch match = new TeamMatch(teamA, teamB, mode);

        plugin.getArenaHandler().startArena(match, new FailablePromise<Arena>() {
            @Override
            public void success(@Nonnull Arena arena) {
                final String nowPlaying = ChatColor.YELLOW + "You are now playing " + ChatColor.AQUA + arena.getName() +
                        ChatColor.YELLOW + " by " + ChatColor.AQUA + Joiner.on(ChatColor.YELLOW + ", " + ChatColor.AQUA).join(arena.getAuthors());

                teamA.getMembers().forEach(member -> mode.giveBooks(member.getPlayer()));
                teamB.getMembers().forEach(member -> mode.giveBooks(member.getPlayer()));

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
