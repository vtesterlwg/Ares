package com.playares.factions.listener;

import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.bukkit.item.custom.event.CustomItemInteractEvent;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.factions.Factions;
import com.playares.factions.claims.builder.DefinedClaimBuilder;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.items.ClaimingStick;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.ProtectionTimer;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public final class ClaimBuilderListener implements Listener {
    @Getter
    public Factions plugin;

    public ClaimBuilderListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final DefinedClaimBuilder builder = plugin.getClaimManager().getClaimBuilder(player);

        if (builder != null) {
            plugin.getClaimManager().getClaimBuilders().remove(builder);
        }
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
                    final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
                    final Faction owner = plugin.getFactionManager().getFactionById(definedClaim.getOwnerId());

                    if (owner == null) {
                        player.sendMessage(ChatColor.RED + "Claim owner not found");
                        return;
                    }

                    if (profile != null) {
                        profile.hideAllClaimPillars();
                    }

                    plugin.getClaimManager().getClaimRepository().add(definedClaim);

                    plugin.getClaimManager().getClaimBuilders().remove(builder);

                    player.getInventory().remove(claimingStick.getItem());

                    if (owner instanceof PlayerFaction) {
                        final PlayerFaction pf = (PlayerFaction)owner;

                        pf.sendMessage(ChatColor.YELLOW + "" + definedClaim.getLxW()[0] + "x" + definedClaim.getLxW()[1] +
                                ChatColor.GOLD + " claim created by " + ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD +
                                " for " + ChatColor.GREEN + "$" + definedClaim.getValue());

                        if (profile != null && profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION) && definedClaim.inside(new PLocatable(player))) {
                            final ProtectionTimer timer = (ProtectionTimer)profile.getTimer(PlayerTimer.PlayerTimerType.PROTECTION);
                            timer.onFinish();
                            profile.getTimers().remove(timer);
                            player.sendMessage(ChatColor.RED + "Your PvP Protection has been removed because you created a claim while standing inside the claim");
                        }
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