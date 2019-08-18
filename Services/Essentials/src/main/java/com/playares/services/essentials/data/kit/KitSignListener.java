package com.playares.services.essentials.data.kit;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.UUID;

public final class KitSignListener implements Listener {
    @Getter public final KitManager manager;
    @Getter public final List<UUID> recentlyEquipped;

    KitSignListener(KitManager manager) {
        this.manager = manager;
        this.recentlyEquipped = Lists.newArrayList();
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final String[] lines = event.getLines();
        final String l1 = lines[0], kitName = lines[1];

        if (!l1.equals("kitsign")) {
            return;
        }

        if (!player.hasPermission("essentials.kits")) {
            return;
        }

        final Kit kit = getManager().getKit(kitName);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found");
            return;
        }

        event.setLine(0, ChatColor.BLUE + "[Load Kit]");
        event.setLine(1, kit.getName());
        event.setLine(2, "");
        event.setLine(3, "");

        Logger.print(player.getName() + " created a kit sign for the kit " + kit.getName());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final Action action = event.getAction();
        final Block block = event.getClickedBlock();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (block == null || !(block.getType().equals(Material.SIGN) || block.getType().equals(Material.WALL_SIGN))) {
            return;
        }

        final Sign sign = (Sign)block.getState();
        final String[] lines = sign.getLines();
        final String l1 = lines[0], kitName = lines[1];

        if (!l1.equals(ChatColor.BLUE + "[Load Kit]")) {
            return;
        }

        if (recentlyEquipped.contains(uniqueId)) {
            player.sendMessage(ChatColor.RED + "Please wait a moment before equipping another kit");
            return;
        }

        final Kit kit = getManager().getKit(kitName);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found");
            return;
        }

        kit.apply(player);
        recentlyEquipped.add(uniqueId);
        player.sendMessage(ChatColor.GREEN + "Loaded " + ChatColor.AQUA + kit.getName());

        new Scheduler(getManager().getPlugin()).sync(() -> getRecentlyEquipped().remove(uniqueId)).delay(3 * 20L).run();
    }
}
