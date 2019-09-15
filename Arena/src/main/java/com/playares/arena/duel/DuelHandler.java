package com.playares.arena.duel;

import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.team.Team;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class DuelHandler {
    @Getter public final DuelManager manager;

    public void openDuelMenu(Player player, String username, SimplePromise promise) {
        final ArenaPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player);
        final Player otherPlayer = Bukkit.getPlayer(username);

        if (otherPlayer == null) {
            promise.failure("Player not found");
            return;
        }

        if (otherPlayer.getUniqueId().equals(player.getUniqueId())) {
            promise.failure("You can not duel yourself");
            return;
        }

        final ArenaPlayer otherProfile = manager.getPlugin().getPlayerManager().getPlayer(otherPlayer);

        if (otherProfile == null) {
            promise.failure("Player not found");
            return;
        }

        final Team team = manager.getPlugin().getTeamManager().getTeam(profile);
        final Team otherTeam = manager.getPlugin().getTeamManager().getTeam(otherProfile);

        if (team != null) {
            promise.failure("You can not send a duel while you are on a team");
            return;
        }

        if (otherTeam != null) {
            promise.failure("This player is in a team");
            return;
        }

        if (!otherProfile.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure(otherPlayer.getName() + " is not in the lobby");
            return;
        }

        final PlayerDuelRequest existing = getManager().getPendingDuelRequest(player, username);

        if (existing != null) {
            promise.failure(ChatColor.RED + "Please wait a moment before sending " + otherProfile.getUsername() + " another duel request");
            return;
        }

        final Menu menu = new Menu(manager.getPlugin(), player, "Duel " + otherProfile.getUsername(), 1);

        for (MatchmakingQueue queues : manager.getPlugin().getQueueManager().getMatchmakingQueues()) {
            menu.addItem(new ClickableItem(queues.getIcon(), queues.getQueueType().getIconPosition(), click -> {
                player.closeInventory();

                final PlayerDuelRequest request = new PlayerDuelRequest(manager.getPlugin(), profile, otherProfile, queues.getQueueType());
                manager.addRequest(request);

                // You have challenged playerName to a No Debuff duel
                // Awaiting their response...

                player.sendMessage(" ");
                player.sendMessage(ChatColor.YELLOW + "You have challenged " + ChatColor.AQUA + otherProfile.getUsername() + ChatColor.YELLOW + " to a " + ChatColor.GOLD + queues.getQueueType().getDisplayName() + ChatColor.YELLOW + " duel");
                player.sendMessage(ChatColor.GRAY + "Awaiting their response...");
                player.sendMessage(" ");

                // johnsama has challenged you to a No Debuff duel! [Accept]
                otherPlayer.sendMessage(
                        new ComponentBuilder(player.getName())
                                .color(net.md_5.bungee.api.ChatColor.AQUA)
                                .append(" has challenged you to a ")
                                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                .append(queues.getQueueType().getDisplayName())
                                .color(net.md_5.bungee.api.ChatColor.GOLD)
                                .append(" duel!")
                                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                .append(" [Accept]")
                                .color(net.md_5.bungee.api.ChatColor.GREEN)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + player.getName()))
                                .create());
            }));
        }

        menu.open();
    }

    public void acceptDuel(Player player, String acceptingUsername, SimplePromise promise) {
        final ArenaPlayer self = manager.getPlugin().getPlayerManager().getPlayer(player);
        final ArenaPlayer accepting = manager.getPlugin().getPlayerManager().getPlayer(acceptingUsername);

        if (self == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        if (accepting == null) {
            promise.failure("Player not found");
            return;
        }

        final Team selfTeam = manager.getPlugin().getTeamManager().getTeam(self);
        final Team acceptingTeam = manager.getPlugin().getTeamManager().getTeam(accepting);

        if (!self.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure("You are not in the lobby");
            return;
        }

        if (!accepting.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure(accepting.getUsername() + " is not in the lobby");
            return;
        }

        if (selfTeam != null) {
            promise.failure("You can not accept a duel request while you are on a team");
            return;
        }

        if (acceptingTeam != null) {
            promise.failure(accepting.getUsername() + " is now on a team");
            return;
        }

        final PlayerDuelRequest request = manager.getAcceptedPlayerDuelRequest(player, acceptingUsername);

        if (request == null) {
            promise.failure(accepting.getUsername() + " has not sent you a duel request");
            return;
        }

        request.accept();
        promise.success();
    }

    public void acceptTeam(Player player, String acceptingUsername, SimplePromise promise) {
        final ArenaPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player);

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        final Team team = manager.getPlugin().getTeamManager().getTeam(profile);
        final Team accepting = manager.getPlugin().getTeamManager().getTeam(acceptingUsername);

        if (team == null) {
            promise.failure("You are not on a team");
            return;
        }

        if (accepting == null) {
            promise.failure("Team not found");
            return;
        }

        final TeamDuelRequest request = manager.getAcceptedTeamDuelRequest(player, acceptingUsername);

        if (request == null) {
            promise.failure("Your team does not have a pending request from Team " + accepting.getLeader().getUsername());
            return;
        }

        if (!request.getRequesting().getStatus().equals(Team.TeamStatus.LOBBY)) {
            promise.failure("Team is no longer in the lobby");
            return;
        }

        if (!request.getRequested().getStatus().equals(Team.TeamStatus.LOBBY)) {
            promise.failure("Your team is not in the lobby");
            return;
        }

        request.accept();
        promise.success();
    }
}
