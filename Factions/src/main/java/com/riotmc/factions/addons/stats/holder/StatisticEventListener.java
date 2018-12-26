package com.riotmc.factions.addons.stats.holder;

import com.riotmc.factions.Factions;
import com.riotmc.factions.factions.data.PlayerFaction;
import com.riotmc.factions.players.data.FactionPlayer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public final class StatisticEventListener implements Listener {
    @Getter public final Factions plugin;

    public StatisticEventListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final Player killer = player.getKiller();
        final FactionPlayer deadProfile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final PlayerFaction deadFaction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        if (deadProfile != null) {
            deadProfile.getStats().addDeath();
        }

        if (deadFaction != null) {
            deadFaction.getStats().addDeath();
        }

        if (killer != null) {
            final FactionPlayer killerProfile = plugin.getPlayerManager().getPlayer(killer.getUniqueId());
            final PlayerFaction killerFaction = plugin.getFactionManager().getFactionByPlayer(killer.getUniqueId());

            if (killerProfile != null) {
                killerProfile.getStats().addKill();
            }

            if (killerFaction != null) {
                killerFaction.getStats().addKill();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (event.isCancelled()) {
            return;
        }

        if (hand == null || block == null) {
            return;
        }

        if (!block.getType().equals(Material.COAL_ORE) && !block.getType().equals(Material.IRON_ORE) && !block.getType().equals(Material.GOLD_ORE) &&
        !block.getType().equals(Material.REDSTONE_ORE) && !block.getType().equals(Material.LAPIS_ORE) && !block.getType().equals(Material.DIAMOND_ORE) &&
        !block.getType().equals(Material.EMERALD_ORE)) {
            return;
        }

        if (hand.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }

        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null) {
            profile.getStats().addOre(block.getType(), block.getDrops(hand).size());
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        profile.getStats().addPlaytime(player.getLastPlayed());
    }
}