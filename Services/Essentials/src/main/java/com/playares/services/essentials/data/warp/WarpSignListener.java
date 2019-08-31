package com.playares.services.essentials.data.warp;

import com.playares.commons.bukkit.logger.Logger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@AllArgsConstructor
public final class WarpSignListener implements Listener {
    @Getter public final WarpManager manager;

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final String[] lines = event.getLines();
        final String l1 = lines[0], warpName = lines[1];

        if (!l1.equals("warpsign")) {
            return;
        }

        if (!player.hasPermission("essentials.warps")) {
            return;
        }

        final Warp warp = getManager().getWarp(warpName);

        if (warp == null) {
            player.sendMessage("Warp not found");
            return;
        }

        event.setLine(0, ChatColor.BLUE + "[Warp]");
        event.setLine(1, warp.getName());
        event.setLine(2, "");
        event.setLine(3, "");

        Logger.print(player.getName() + " created a warp sign for the warp " + warp.getName());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();
        final Block block = event.getClickedBlock();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (block == null || !block.getType().name().contains("SIGN")) {
            return;
        }

        final Sign sign = (Sign)block.getState();
        final String[] lines = sign.getLines();
        final String l1 = lines[0], warpName = lines[1];

        if (!l1.equals(ChatColor.BLUE + "[Warp]")) {
            return;
        }

        final Warp warp = getManager().getWarp(warpName);

        if (warp == null) {
            player.sendMessage(ChatColor.RED + "Warp not found");
            return;
        }

        warp.teleport(player);
        player.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.AQUA + warp.getName());
    }
}
