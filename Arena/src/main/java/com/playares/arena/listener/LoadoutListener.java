package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.loadout.Loadout;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class LoadoutListener implements Listener {
    @Getter
    public final Arenas plugin;

    public LoadoutListener(Arenas plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack hand = player.getInventory().getItemInMainHand();
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        if (hand == null || !hand.hasItemMeta() || !hand.getType().equals(Material.ENCHANTED_BOOK)) {
            return;
        }

        final String loadoutName = ChatColor.stripColor(hand.getItemMeta().getDisplayName());
        final Loadout loadout = plugin.getLoadoutManager().getLoadout(loadoutName);

        if (loadout == null) {
            player.sendMessage(ChatColor.RED + "Loadout not found");
            return;
        }

        loadout.apply(player);
        player.sendMessage(ChatColor.YELLOW + "Loaded Loadout" + ChatColor.WHITE + ": " + loadout.getName());
    }
}