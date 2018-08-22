package com.playares.factions.listener;

import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.bukkit.item.custom.event.CustomItemInteractEvent;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.Factions;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.claims.builder.DefinedClaimBuilder;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.items.ClaimingStick;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class ClaimBuilderListener implements Listener {
    @Getter
    public Factions plugin;

    public ClaimBuilderListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);
        final Player player = event.getPlayer();
        final ItemStack item = event.getItemDrop().getItemStack();

        if (customItemService == null) {
            return;
        }

        customItemService.getItem(item).ifPresent(customItem -> {
            if (customItem instanceof ClaimingStick) {
                final DefinedClaimBuilder builder = plugin.getClaimManager().getClaimBuilder(player);

                event.getItemDrop().remove();

                if (builder != null) {
                    builder.resetCorners();
                    plugin.getClaimManager().getClaimBuilders().remove(builder);
                }
            }
        });
    }

    @EventHandler
    public void onCustomItemInteract(CustomItemInteractEvent event) {
        if (!(event.getItem() instanceof ClaimingStick)) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();
        final boolean sneaking = player.isSneaking();
        final ClaimingStick claimingStick = (ClaimingStick)event.getItem();
        final DefinedClaimBuilder builder = plugin.getClaimManager().getClaimBuilder(player);

        event.setCancelled(true);

        if (builder == null) {
            player.getInventory().remove(claimingStick.getItem());
            return;
        }

        if (action.equals(Action.LEFT_CLICK_BLOCK) && block != null) {
            builder.setCornerA(new BLocatable(block));
            return;
        }

        if (action.equals(Action.RIGHT_CLICK_BLOCK) && block != null) {
            builder.setCornerB(new BLocatable(block));
            return;
        }

        if (action.equals(Action.LEFT_CLICK_AIR) && sneaking) {
            builder.build(new FailablePromise<DefinedClaim>() {
                @Override
                public void success(@Nonnull DefinedClaim definedClaim) {
                    final Faction owner = plugin.getFactionManager().getFactionById(definedClaim.getOwnerId());

                    if (owner == null) {
                        player.sendMessage(ChatColor.RED + "Claim owner not found");
                        return;
                    }

                    plugin.getClaimManager().getClaimRepository().add(definedClaim);

                    if (owner instanceof PlayerFaction) {
                        final PlayerFaction pf = (PlayerFaction)owner;

                        pf.sendMessage(ChatColor.YELLOW + "" + definedClaim.getLxW()[0] + "x" + definedClaim.getLxW()[1] +
                                ChatColor.GOLD + " claim created by " + ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD +
                                " for " + ChatColor.GREEN + "$" + definedClaim.getValue());
                    }

                    player.sendMessage(ChatColor.GREEN + "Claim created");
                }

                @Override
                public void failure(@Nonnull String reason) {
                   player.sendMessage(ChatColor.RED + reason);
                }
            });

            return;
        }

        if (action.equals(Action.RIGHT_CLICK_AIR) && sneaking) {
            builder.resetCorners();
            return;
        }
    }
}