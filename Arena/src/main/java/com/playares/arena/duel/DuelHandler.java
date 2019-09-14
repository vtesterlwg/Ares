package com.playares.arena.duel;

import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class DuelHandler {
    @Getter public final DuelManager manager;

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
