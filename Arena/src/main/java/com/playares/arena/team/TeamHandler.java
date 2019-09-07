package com.playares.arena.team;

import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class TeamHandler {
    @Getter public final TeamManager manager;

    TeamHandler(TeamManager manager) {
        this.manager = manager;
    }

    public void create(ArenaPlayer player, SimplePromise promise) {
        if (player.getPlayer() == null) {
            promise.failure("Player is not online");
            return;
        }

        final Team team = new Team(manager.getPlugin(), player);
        manager.getTeams().add(team);

        promise.success();
    }

    public void disband(ArenaPlayer player, SimplePromise promise) {
        final Team team = manager.getTeam(player);

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (!team.isLeader(player) && !player.getPlayer().hasPermission("arena.admin")) {
            promise.failure("You must be the leader of the team to perform this action");
            return;
        }

        if (!team.getStatus().equals(Team.TeamStatus.LOBBY)) {
            promise.failure("Team can not be disbanded while you are in a match");
            return;
        }

        manager.getTeams().remove(team);

        team.getAvailableMembers().forEach(member -> manager.getPlugin().getPlayerManager().getHandler().giveItems(member));
        team.sendMessage(ChatColor.RED + "Team has been disbanded by " + player.getUsername());
        team.getMembers().clear();
        team.getInvitations().clear();
    }

    public void open(Player bukkitPlayer, SimplePromise promise) {
        final ArenaPlayer player = manager.getPlugin().getPlayerManager().getPlayer(bukkitPlayer);

        if (player == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        final Team team = manager.getTeam(player);

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (!team.isLeader(player) && !bukkitPlayer.hasPermission("arena.admin")) {
            promise.failure("You must be the leader of the team to perform this action");
            return;
        }

        if (team.isOpen()) {
            promise.failure("Team is already open");
            return;
        }

        team.setOpen(true);
        team.sendMessage(ChatColor.YELLOW + "Team is now " + ChatColor.GREEN + "open");

        promise.success();
    }

    public void close(Player bukkitPlayer, SimplePromise promise) {
        final ArenaPlayer player = manager.getPlugin().getPlayerManager().getPlayer(bukkitPlayer);

        if (player == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        final Team team = manager.getTeam(player);

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (!team.isLeader(player) && !bukkitPlayer.hasPermission("arena.admin")) {
            promise.failure("You must be the leader of the team to perform this action");
            return;
        }

        if (!team.isOpen()) {
            promise.failure("Team is already closed");
            return;
        }

        team.setOpen(false);
        team.sendMessage(ChatColor.YELLOW + "Team is now " + ChatColor.RED + "closed");

        promise.success();
    }

    public void invite(Player bukkitInviter, String username, SimplePromise promise) {
        final ArenaPlayer inviter = manager.getPlugin().getPlayerManager().getPlayer(bukkitInviter);

        if (inviter == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        final Team team = manager.getTeam(inviter);

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (!team.isLeader(inviter)) {
            promise.failure("You must be the leader to perform this action");
            return;
        }

        final ArenaPlayer invited = manager.getPlugin().getPlayerManager().getPlayer(username);

        if (invited == null) {
            promise.failure("Player not found");
            return;
        }

        if (!invited.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure(invited.getUsername() + " is not in the lobby");
            return;
        }

        if (manager.getTeam(invited) != null) {
            promise.failure(invited.getUsername() + " is already on a team");
            return;
        }

        if (team.getInvitations().contains(invited)) {
            promise.failure(invited.getUsername() + " already has a pending invitation to join your team");
            return;
        }

        team.getInvitations().add(invited);
        team.sendMessage(ChatColor.AQUA + inviter.getUsername() + ChatColor.YELLOW + " has invited " + ChatColor.AQUA + invited.getUsername() + ChatColor.YELLOW + " to join the team");

        invited.getPlayer().sendMessage(new ComponentBuilder(inviter.getUsername())
        .color(net.md_5.bungee.api.ChatColor.AQUA)
        .append(" has sent you an invitation to join their team! ")
        .color(net.md_5.bungee.api.ChatColor.YELLOW)
        .append("[Click here to join]")
        .color(net.md_5.bungee.api.ChatColor.BLUE)
        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team join " + inviter.getUsername()))
        .create());

        new Scheduler(manager.getPlugin()).sync(() -> team.getInvitations().remove(invited)).delay(30 * 20L).run(); // TODO: Make configurable

        promise.success();
    }

    public void accept(Player bukkitPlayer, String username, SimplePromise promise) {
        final ArenaPlayer player = manager.getPlugin().getPlayerManager().getPlayer(bukkitPlayer);

        if (player == null) {
            promise.failure("Player not found");
            return;
        }

        final Team team = manager.getTeam(username);
        final Team existing = manager.getTeam(player);

        if (existing != null) {
            promise.failure("You are already on a team");
            return;
        }

        if (team == null) {
            promise.failure("Team not found");
            return;
        }

        if (!team.isOpen() && !team.getInvitations().contains(player) && !bukkitPlayer.hasPermission("arena.admin")) {
            promise.failure("You do not have an invitation to join this team");
            return;
        }

        if (!team.getStatus().equals(Team.TeamStatus.LOBBY)) {
            promise.failure("Team is not in the lobby right now");
            return;
        }

        team.getInvitations().remove(player);
        team.getMembers().add(player);
        team.sendMessage(ChatColor.AQUA + player.getUsername() + ChatColor.YELLOW + " has " + ChatColor.GREEN + "joined" + ChatColor.YELLOW + " the team!");

        manager.getPlugin().getPlayerManager().getHandler().giveItems(player);

        promise.success();
    }

    public void leave(ArenaPlayer player, SimplePromise promise) {
        if (!player.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure("You can not leave the team until you are in the lobby");
            return;
        }

        final Team team = manager.getTeam(player);

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        team.getMembers().remove(player);
        team.sendMessage(ChatColor.AQUA + player.getUsername() + ChatColor.YELLOW + " has " + ChatColor.RED + "left" + ChatColor.YELLOW + " the team");

        manager.getPlugin().getPlayerManager().getHandler().giveItems(player);

        promise.success();
    }

    public void leave(Player bukkitPlayer, SimplePromise promise) {
        final ArenaPlayer player = manager.getPlugin().getPlayerManager().getPlayer(bukkitPlayer);
        leave(player, promise);
    }

    public void kick(Player bukkitPlayer, String username, SimplePromise promise) {
        final ArenaPlayer player = manager.getPlugin().getPlayerManager().getPlayer(bukkitPlayer);

        if (player == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        final Team team = manager.getTeam(player);

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (!team.getLeader().equals(player) && !bukkitPlayer.hasPermission("arena.admin")) {
            promise.failure("You must be the leader to perform this action");
            return;
        }

        if (!team.getStatus().equals(Team.TeamStatus.LOBBY)) {
            promise.failure("Can not kick player while your team is in a match");
            return;
        }

        final ArenaPlayer kicked = manager.getPlugin().getPlayerManager().getPlayer(username);

        if (kicked == null) {
            promise.failure("Player not found");
            return;
        }

        if (!team.getMembers().contains(kicked)) {
            promise.failure(kicked.getUsername() + " is not on your team");
            return;
        }

        if (!kicked.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure("This player is not in the lobby");
            return;
        }

        team.getMembers().remove(kicked);
        team.sendMessage(ChatColor.AQUA + kicked.getUsername() + ChatColor.YELLOW + " has been " + ChatColor.RED + "kicked" + ChatColor.YELLOW + " by " + ChatColor.AQUA + player.getUsername());

        manager.getPlugin().getPlayerManager().getHandler().giveItems(player);

        promise.success();
    }
}