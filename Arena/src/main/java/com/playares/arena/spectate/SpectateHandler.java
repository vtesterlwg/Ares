package com.playares.arena.spectate;

import com.playares.arena.match.Match;
import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class SpectateHandler {
    @Getter public final SpectateManager manager;

    public void spectate(Player spectator, String username, SimplePromise promise) {
        final Player player = Bukkit.getPlayer(username);

        if (player == null) {
            promise.failure("Player not found");
            return;
        }

        final ArenaPlayer playerProfile = manager.getPlugin().getPlayerManager().getPlayer(player);
        final ArenaPlayer spectatorProfile = manager.getPlugin().getPlayerManager().getPlayer(spectator);

        if (playerProfile == null) {
            promise.failure("Player not found");
            return;
        }

        if (spectatorProfile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        if (spectatorProfile.getStatus().equals(ArenaPlayer.PlayerStatus.SPECTATING)) {
            promise.failure("You are already spectating. Leave this session by right-clicking the 'Stop Spectating' item and use this command again");
            return;
        }

        if (!spectatorProfile.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure("You must be in the lobby to perform this command");
            return;
        }

        final Match match = manager.getPlugin().getMatchManager().getMatchByPlayer(playerProfile);

        if (match == null || playerProfile.getStatus().equals(ArenaPlayer.PlayerStatus.LOBBY)) {
            promise.failure(player.getName() + " is not in a match");
            return;
        }

        match.addSpectator(spectator);
    }

    public void stopSpectating(Player player, SimplePromise promise) {
        final ArenaPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player);

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        final Match match = manager.getPlugin().getMatchManager().getSpectatingMatch(profile);

        if (match == null) {
            promise.failure("You are not spectating a match");
            return;
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        profile.setStatus(ArenaPlayer.PlayerStatus.LOBBY);

        match.removeFromScoreboards(player);
        match.getPlugin().getPlayerManager().getHandler().giveItems(profile);
        match.getSpectators().remove(profile);

        // TODO: Teleport to lobby

        match.getPlayers().forEach(otherPlayer -> otherPlayer.getPlayer().showPlayer(manager.getPlugin(), player));
    }
}
