package com.playares.arena.duel;

import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class DuelHandler {
    @Getter public final DuelManager manager;

    public void openDuelMenu(Player player, String username, SimplePromise promise) {
        final Player otherPlayer = Bukkit.getPlayer(username);

        if (otherPlayer == null) {
            promise.failure("Player not found");
            return;
        }

        final ArenaPlayer otherProfile = manager.getPlugin().getPlayerManager().getPlayer(otherPlayer);

        if (otherProfile == null) {
            promise.failure("Player not found");
            return;
        }

        if (!otherProfile.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure(otherPlayer.getName() + " is not in the lobby");
            return;
        }

        // TODO: Get existing duel request

        //final Menu menu = new Menu(manager.getPlugin(), player, "Duel " +)
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

        if (!self.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure("You are not in the lobby");
            return;
        }

        if (!accepting.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure(accepting.getUsername() + " is not in the lobby");
            return;
        }

        final DuelRequest request = manager.getAcceptedPlayerDuelRequest(player, acceptingUsername);

        if (request == null) {
            promise.failure(accepting.getUsername() + " has not sent you a duel request");
            return;
        }

        request.accept();
        promise.success();
    }

    public void acceptTeam(Player player, String acceptingUsername, SimplePromise promise) {

    }
}
