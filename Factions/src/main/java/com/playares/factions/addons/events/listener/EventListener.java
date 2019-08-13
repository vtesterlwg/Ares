package com.playares.factions.addons.events.listener;

import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class EventListener implements Listener {
    @Getter public final EventsAddon addon;

    public EventListener(EventsAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (block == null || !block.getType().equals(Material.CHEST)) {
            return;
        }

        final AresEvent eventByChest = getAddon().getManager().getEventByLootChest(new BLocatable(block));

        if (eventByChest == null) {
            return;
        }

        if (eventByChest instanceof KOTHEvent) {
            final KOTHEvent koth = (KOTHEvent)eventByChest;

            if (koth.getSession() == null) {
                return;
            }

            if (koth.getSession().getTimeUntilCaptureChestUnlock() < 0) {
                return;
            }

            if (koth.getSession().isCaptured() && koth.getSession().getCapturingFaction().getMember(player.getUniqueId()) == null) {
                player.sendMessage(ChatColor.RED + "This chest will unlock in " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(koth.getSession().getTimeUntilCaptureChestUnlock()) + ChatColor.RED + "s");
                event.setCancelled(true);
            }
        }
    }
}
