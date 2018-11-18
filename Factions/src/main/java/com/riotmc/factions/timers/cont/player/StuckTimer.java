package com.riotmc.factions.timers.cont.player;

import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.Factions;
import com.riotmc.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a players stuck timer
 */
public final class StuckTimer extends PlayerTimer {
    @Getter
    public final Factions plugin;

    public StuckTimer(Factions plugin, UUID owner, int seconds) {
        super(owner, PlayerTimerType.STUCK, seconds);
        this.plugin = plugin;
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(owner);

        if (player == null) {
            return;
        }

        new Scheduler(plugin).async(() -> {
            final PLocatable location = new PLocatable(player);

            while (plugin.getClaimManager().getClaimAt(location) != null) {
                location.setX(location.getX() + 1.0);
                location.setZ(location.getZ() + 1.0);
            }

            new Scheduler(plugin).sync(() -> {
                final Block block = location.getBukkit().getWorld().getHighestBlockAt((int)(Math.round(location.getX())), (int)(Math.round(location.getZ())));
                final Location fixed = block.getLocation().add(0, 2.0, 0);

                player.teleport(fixed);
                player.sendMessage(ChatColor.GREEN + "You have been teleported outside the claim");

                Logger.print(player.getName() + " finished the unstuck process");
            }).run();
        }).run();
    }
}