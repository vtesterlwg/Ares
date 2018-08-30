package com.playares.factions.claims.handler;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.factions.claims.ClaimManager;
import com.playares.factions.claims.builder.DefinedClaimBuilder;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.items.ClaimingStick;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class ClaimCreationHandler {
    @Getter
    public final ClaimManager manager;

    public ClaimCreationHandler(ClaimManager manager) {
        this.manager = manager;
    }

    public void startClaiming(Player player, SimplePromise promise) {
        final CustomItemService customItemService = (CustomItemService)manager.getPlugin().getService(CustomItemService.class);
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getFactionByPlayer(player.getUniqueId());
        final boolean admin = player.hasPermission("factions.admin");

        if (customItemService == null) {
            promise.failure("Failed to obtain Custom Item Service");
            return;
        }

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !admin) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        if (manager.getClaimBuilder(player) != null) {
            promise.failure("You are already claiming");
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            promise.failure("Inventory is full");
            return;
        }

        final DefinedClaimBuilder builder = new DefinedClaimBuilder(manager.getPlugin(), faction, player);
        final Optional<CustomItem> claimingStickResult = customItemService.getItem(ClaimingStick.class);

        if (!claimingStickResult.isPresent()) {
            promise.failure("Failed to find Claiming Stick");
            return;
        }

        manager.getClaimBuilders().add(builder);

        player.getInventory().addItem(claimingStickResult.get().getItem());

        promise.success();
    }

    public void startClaiming(Player player, String name, SimplePromise promise) {
        final CustomItemService customItemService = (CustomItemService)manager.getPlugin().getService(CustomItemService.class);
        final Faction faction = manager.getPlugin().getFactionManager().getFactionByName(name);

        if (customItemService == null) {
            promise.failure("Failed to obtain Custom Item Service");
            return;
        }

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (manager.getClaimBuilder(player) != null) {
            promise.failure("You are already claiming");
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            promise.failure("Inventory is full");
            return;
        }

        final DefinedClaimBuilder builder = new DefinedClaimBuilder(manager.getPlugin(), faction, player);
        final Optional<CustomItem> claimingStickResult = customItemService.getItem(ClaimingStick.class);

        if (!claimingStickResult.isPresent()) {
            promise.failure("Failed to find Claiming Stick");
            return;
        }

        manager.getClaimBuilders().add(builder);

        player.getInventory().addItem(claimingStickResult.get().getItem());

        promise.success();
    }
}
