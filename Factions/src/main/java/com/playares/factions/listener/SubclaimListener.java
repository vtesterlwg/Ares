package com.playares.factions.listener;

import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.claims.subclaims.dao.SubclaimDAO;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.claims.subclaims.menu.SubclaimMenu;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public final class SubclaimListener implements Listener {
    @Getter public final Factions plugin;

    private void handleBlockEvents(Cancellable event, Player player, Block block) {
        final Subclaim subclaim = plugin.getSubclaimManager().getSubclaimAt(block);

        if (subclaim == null) {
            return;
        }

        final PlayerFaction faction = subclaim.getFaction();

        if (faction == null || faction.isRaidable()) {
            return;
        }

        if (player.hasPermission("factions.subclaim.bypass")) {
            return;
        }

        if (!subclaim.canAccess(player.getUniqueId()) && faction.isMember(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "You do " + ChatColor.RED + "not" + ChatColor.YELLOW + " have access to this subclaim");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final SubclaimMenu activeMenu = getPlugin().getSubclaimManager().getActiveMenu(player);

        if (activeMenu != null) {
            getPlugin().getSubclaimManager().getUpdateHandler().closeMenu(activeMenu);
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        if (event.isCancelled()) {
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) || block == null || !block.getType().name().contains("CHEST")) {
            return;
        }

        handleBlockEvents(event, player, block);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (event.isCancelled()) {
            return;
        }

        handleBlockEvents(event, player, block);

        if (event.isCancelled()) {
            return;
        }

        final Subclaim subclaim = plugin.getSubclaimManager().getSubclaimAt(block);

        if (subclaim == null) {
            return;
        }

        if (!event.isCancelled()) {
            getPlugin().getSubclaimManager().getSubclaimRepository().remove(subclaim);
            new Scheduler(getPlugin()).async(() -> SubclaimDAO.deleteSubclaim(getPlugin().getMongo(), subclaim)).run();
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block sourceBlock = event.getSource().getLocation().getBlock();
        final Block destinationBlock = event.getDestination().getLocation().getBlock();

        if (sourceBlock != null && sourceBlock.getType().name().contains("CHEST")) {
            final Subclaim subclaimSource = getPlugin().getSubclaimManager().getSubclaimAt(sourceBlock);

            if (subclaimSource != null && subclaimSource.getFaction() != null && !subclaimSource.getFaction().isRaidable()) {
                event.setCancelled(true);
                return;
            }
        }

        if (destinationBlock != null && destinationBlock.getType().name().contains("CHEST")) {
            final Subclaim subclaimDestination = getPlugin().getSubclaimManager().getSubclaimAt(destinationBlock);

            if (subclaimDestination != null && subclaimDestination.getFaction() != null && !subclaimDestination.getFaction().isRaidable()) {
                event.setCancelled(true);
            }
        }
    }
}