package com.playares.arena.team;

import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;

public final class TeamHandler {
    @Getter
    public final Arenas plugin;

    public TeamHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void createTeam(ArenaPlayer player, FailablePromise<Team> promise) {
        if (player.getTeam() != null) {
            promise.failure("You are already on a team");
            return;
        }

        if (!player.getStatus().equals(PlayerStatus.LOBBY)) {
            promise.failure("You are not in the lobby");
            return;
        }

        final Team team = new Team(player);

        player.setTeam(team);
        player.getPlayer().setScoreboard(team.getScoreboard().getScoreboard());

        promise.success(team);
    }

    public void leaveTeam(ArenaPlayer player, SimplePromise promise) {
        final Team team = player.getTeam();

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        team.getMembers().remove(player);
        team.getScoreboard().getFriendlyTeam().removeEntry(player.getUsername());
        team.sendMessage(ChatColor.AQUA + player.getUsername() + ChatColor.YELLOW + " has left the team");

        player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.setTeam(null);

        if (team.getLeader().equals(player)) {
            if (team.getMembers().size() == 0) {
                plugin.getTeamManager().getTeams().remove(team);
            } else {
                final ArenaPlayer newLeader = team.getMembers().iterator().next();
                team.setLeader(newLeader);
                team.sendMessage(ChatColor.AQUA + newLeader.getUsername() + ChatColor.YELLOW + " has been promoted to leader");
            }
        }

        promise.success();
    }

    public void disbandTeam(ArenaPlayer player, SimplePromise promise) {
        final Team team = player.getTeam();

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        team.sendMessage(ChatColor.RED + "Team has been disbanded");
        team.getMembers().forEach(member -> {
            member.setTeam(null);
            member.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            plugin.getPlayerHandler().giveLobbyItems(member);
        });

        plugin.getTeamManager().getTeams().remove(team);

        promise.success();
    }

    public void openTeam(ArenaPlayer player, SimplePromise promise) {
        final Team team = player.getTeam();

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (!team.getLeader().equals(player)) {
            promise.failure("You must be the team leader to perform this action");
            return;
        }

        if (!team.getStatus().equals(TeamStatus.LOBBY)) {
            promise.failure("Your team must be in the lobby to perform this action");
            return;
        }

        if (team.isOpen()) {
            promise.failure("Your team is already open");
            return;
        }

        team.setOpen(true);
        team.sendMessage(ChatColor.AQUA + player.getUsername() + ChatColor.YELLOW + " has opened the team for any player to join");
        promise.success();
    }

    public void closeTeam(ArenaPlayer player, SimplePromise promise) {
        final Team team = player.getTeam();

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (!team.getLeader().equals(player)) {
            promise.failure("You must be the team leader to perform this action");
            return;
        }

        if (!team.getStatus().equals(TeamStatus.LOBBY)) {
            promise.failure("Your team must be in the lobby to perform this action");
            return;
        }

        if (!team.isOpen()) {
            promise.failure("Your team is already closed");
            return;
        }

        team.setOpen(false);
        team.sendMessage(ChatColor.AQUA + player.getUsername() + ChatColor.YELLOW + " has closed the team. Players must now be invited to join.");
        promise.success();
    }

    public void invitePlayer(ArenaPlayer player, ArenaPlayer invited, SimplePromise promise) {
        final Team team = player.getTeam();

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (!team.getLeader().equals(player)) {
            promise.failure("You must be the leader of the team to perform this action");
            return;
        }

        if (invited.getTeam() != null) {
            promise.failure("This player is already on a team");
            return;
        }

        if (team.hasInvite(invited)) {
            promise.failure("This player already has a pending invite to join your team");
            return;
        }

        team.getInvites().add(invited);
        team.sendMessage(ChatColor.AQUA + player.getUsername() + ChatColor.YELLOW + " invited " +
                ChatColor.AQUA + invited.getUsername() + ChatColor.YELLOW + " to the team");

        invited.getPlayer().sendMessage(new ComponentBuilder(player.getUsername())
                .color(ChatColor.AQUA)
                .append(" has invited you to join their team! ")
                .color(ChatColor.YELLOW)
                .append("Click here to join!")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team join " + player.getUsername()))
                .create());

        promise.success();

        new Scheduler(plugin).sync(() -> team.getInvites().remove(invited)).delay(30 * 20L).run();
    }

    public void joinTeam(ArenaPlayer player, ArenaPlayer target, SimplePromise promise) {
        final Team team = target.getTeam();

        if (!player.getStatus().equals(PlayerStatus.LOBBY)) {
            promise.failure("You must be in the lobby to perform this action");
            return;
        }

        if (player.getTeam() != null) {
            promise.failure("You are already on a team");
            return;
        }

        if (team == null) {
            promise.failure("Team not found");
            return;
        }

        if (!team.hasInvite(player) && !team.isOpen()) {
            promise.failure("You do not have an invite to this team");
            return;
        }

        if (!team.getStatus().equals(TeamStatus.LOBBY)) {
            promise.failure("Team is not in the lobby");
            return;
        }

        team.getInvites().remove(player);
        team.getMembers().add(player);
        team.getScoreboard().getFriendlyTeam().addEntry(player.getUsername());
        team.sendMessage(ChatColor.AQUA + player.getUsername() + ChatColor.YELLOW + " joined the team");

        player.setTeam(team);
        player.getPlayer().setScoreboard(team.getScoreboard().getScoreboard());
        plugin.getPlayerHandler().giveLobbyItems(player);

        promise.success();
    }
}
