package com.playares.factions.claims.subclaims.handler;

import com.google.common.collect.Lists;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.claims.subclaims.manager.SubclaimManager;
import com.playares.factions.claims.subclaims.menu.SubclaimMenu;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

import java.util.List;

@AllArgsConstructor
public final class SubclaimCreationHandler {
    @Getter public final SubclaimManager manager;

    public void create(Player player, SimplePromise promise) {
        final PlayerFaction faction = getManager().getPlugin().getFactionManager().getFactionByPlayer(player.getUniqueId());
        final Block target = player.getTargetBlock(null, 4);

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()) == null || faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER)) {
            promise.failure("You do not have a high enough faction rank to perform this action");
            return;
        }

        if (target == null || !target.getType().name().contains("CHEST")) {
            promise.failure("This block is not a chest");
            return;
        }

        final BlockState state = target.getState();
        final List<Block> blocks = Lists.newArrayList();
        final List<BLocatable> convertedBlocks = Lists.newArrayList();

        if (state instanceof Chest) {
            final Chest chest = (Chest)state;
            final Inventory inventory = chest.getInventory();

            if (inventory instanceof DoubleChestInventory) {
                final DoubleChest doubleChest = (DoubleChest)inventory.getHolder();
                final Chest left = (Chest)doubleChest.getLeftSide();
                final Chest right = (Chest)doubleChest.getRightSide();

                blocks.add(left.getBlock());
                blocks.add(right.getBlock());
            } else {
                blocks.add(chest.getBlock());
            }
        }

        if (blocks.isEmpty()) {
            promise.failure("No blocks found");
            return;
        }

        for (Block block : blocks) {
            final Subclaim subclaimAt = getManager().getSubclaimAt(block);
            final DefinedClaim claimAt = getManager().getPlugin().getClaimManager().getClaimAt(new BLocatable(block));

            if (claimAt == null || !claimAt.getOwnerId().equals(faction.getUniqueId())) {
                promise.failure("This chest is not within your factions claims");
                return;
            }

            if (subclaimAt != null) {
                if (subclaimAt.canAccess(player.getUniqueId())) {
                    getManager().getUpdateHandler().openMenu(new SubclaimMenu(getManager().getPlugin(), player, subclaimAt));
                    promise.success();
                    return;
                }

                promise.failure("You do not have access to this subclaim");
                return;
            }

            convertedBlocks.add(new BLocatable(block));
        }

        final Subclaim subclaim = new Subclaim(getManager().getPlugin(), player, faction, convertedBlocks);
        getManager().getSubclaimRepository().add(subclaim);
        getManager().getUpdateHandler().openMenu(new SubclaimMenu(getManager().getPlugin(), player, subclaim));
        promise.success();
    }
}
