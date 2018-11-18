package com.riotmc.factions.addons.stats.lore;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class TrackableItemListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final Player killer = player.getKiller();

        if (killer == null) {
            return;
        }

        final ItemStack hand = killer.getInventory().getItemInMainHand();

        if (hand == null) {
            return;
        }

        final TrackableType type = TrackableType.getTypeByItem(hand);

        if (type == null) {
            return;
        }

        if (type.equals(TrackableType.SWORD)) {
            final TrackableSword trackable = new TrackableSword(hand);

            trackable.setKills(trackable.getKills() + 1);
            trackable.update();
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
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

        if (!block.getType().equals(Material.COAL_ORE) && !block.getType().equals(Material.IRON_ORE) && !block.getType().equals(Material.REDSTONE_ORE)
        && !block.getType().equals(Material.LAPIS_ORE) && !block.getType().equals(Material.GOLD_ORE) && !block.getType().equals(Material.DIAMOND_ORE)
        && !block.getType().equals(Material.EMERALD_ORE)) {
            return;
        }

        if (hand.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }

        final TrackableType type = TrackableType.getTypeByItem(hand);

        if (type == null) {
            return;
        }

        if (type.equals(TrackableType.PICKAXE)) {
            final TrackablePickaxe trackable = new TrackablePickaxe(hand);

            if (block.getType().equals(Material.COAL_ORE)) {
                trackable.setCoal(trackable.getCoal() + block.getDrops(hand).size());
            } else if (block.getType().equals(Material.IRON_ORE)) {
                trackable.setIron(trackable.getIron() + 1);
            } else if (block.getType().equals(Material.REDSTONE_ORE)) {
                trackable.setRedstone(trackable.getRedstone() + block.getDrops(hand).size());
            } else if (block.getType().equals(Material.LAPIS_ORE)) {
                trackable.setLapis(trackable.getLapis() + block.getDrops(hand).size());
            } else if (block.getType().equals(Material.GOLD_ORE)) {
                trackable.setGold(trackable.getGold() + 1);
            } else if (block.getType().equals(Material.DIAMOND_ORE)) {
                trackable.setDiamond(trackable.getDiamond() + block.getDrops(hand).size());
            } else if (block.getType().equals(Material.EMERALD_ORE)) {
                trackable.setEmerald(trackable.getEmerald() + block.getDrops(hand).size());
            }

            trackable.update();
        }
    }
}
