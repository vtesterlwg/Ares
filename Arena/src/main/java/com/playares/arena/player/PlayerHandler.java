package com.playares.arena.player;

import com.google.common.base.Preconditions;
import com.playares.arena.item.*;
import com.playares.arena.queue.SearchingPlayer;
import com.playares.arena.team.Team;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Players;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class PlayerHandler {
    @Getter public final PlayerManager manager;

    PlayerHandler(PlayerManager manager) {
        this.manager = manager;
    }

    public void giveItems(ArenaPlayer player) {
        Preconditions.checkArgument(player.getPlayer() != null, "Player not found!");

        final Player bukkitPlayer = player.getPlayer();
        final Team team = manager.getPlugin().getTeamManager().getTeam(player);
        final SearchingPlayer search = manager.getPlugin().getQueueManager().getCurrentSearch(player.getPlayer());
        final CustomItemService customItemService = (CustomItemService)manager.getPlugin().getService(CustomItemService.class);

        if (customItemService == null) {
            Logger.error("Failed to give items to " + bukkitPlayer.getName() + " because the Custom Item Service could not be found!");
            return;
        }

        Players.resetHealth(bukkitPlayer);
        Players.resetFlySpeed(bukkitPlayer);
        Players.resetWalkSpeed(bukkitPlayer);
        bukkitPlayer.getInventory().clear();
        bukkitPlayer.getInventory().setArmorContents(null);

        if (player.getStatus().equals(ArenaPlayer.PlayerStatus.SPECTATING)) {
            customItemService.getItem(LeaveSpectatorItem.class).ifPresent(item -> bukkitPlayer.getInventory().setItem(4, item.getItem()));
            return;
        }

        if (team != null) {
            customItemService.getItem(LeaveDisbandTeamItem.class).ifPresent(item -> bukkitPlayer.getInventory().setItem(2, item.getItem()));
            customItemService.getItem(OtherTeamItem.class).ifPresent(item -> bukkitPlayer.getInventory().setItem(6, item.getItem()));
            return;
        }

        if (search != null) {
            customItemService.getItem(LeaveQueueItem.class).ifPresent(item -> bukkitPlayer.getInventory().setItem(4, item.getItem()));
            return;
        }

        customItemService.getItem(UnrankedQueueItem.class).ifPresent(item -> bukkitPlayer.getInventory().setItem(0, item.getItem()));
        customItemService.getItem(RankedQueueItem.class).ifPresent(item -> bukkitPlayer.getInventory().setItem(1, item.getItem()));
        customItemService.getItem(CreateTeamItem.class).ifPresent(item -> bukkitPlayer.getInventory().setItem(4, item.getItem()));
    }
}
