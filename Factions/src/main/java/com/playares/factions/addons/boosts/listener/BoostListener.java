package com.playares.factions.addons.boosts.listener;

import com.google.common.collect.Lists;
import com.playares.factions.addons.boosts.BoostAddon;
import com.playares.factions.addons.boosts.data.Boost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public final class BoostListener implements Listener {
    @Getter public final BoostAddon addon;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (addon.getActiveBoost() != null && !addon.getActiveBoost().isExpired()) {
            player.sendMessage(ChatColor.AQUA + "Server is being boosted with " + ChatColor.RESET + addon.getActiveBoost().getBoost().getType().getDisplayName() +
                    ChatColor.AQUA + " by " + ChatColor.RESET + addon.getActiveBoost().getUsername());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onMobDrops(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player || event.getEntity() instanceof Villager) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (addon.getActiveBoost() != null && addon.getActiveBoost().getBoost().getType().equals(Boost.BoostType.DROPS)) {
            final List<ItemStack> originalDrops = event.getDrops();
            final List<ItemStack> newDrops = Lists.newArrayList();

            originalDrops.forEach(drop -> {
                drop.setAmount(drop.getAmount() * 2);
                newDrops.add(drop);
            });

            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
            return;
        }

        if (addon.getActiveBoost() != null && addon.getActiveBoost().getBoost().getType().equals(Boost.BoostType.EXP)) {
            final int originalExp = event.getDroppedExp();
            final int newExp = originalExp * 2;

            event.setDroppedExp(newExp);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onOreMined(BlockBreakEvent event) {
        final Block block = event.getBlock();

        if (event.isCancelled()) {
            return;
        }

        if (block == null || block.getType().equals(Material.AIR)) {
            return;
        }

        if (addon.getActiveBoost() != null && addon.getActiveBoost().getBoost().getType().equals(Boost.BoostType.EXP)) {
            final int originalExp = event.getExpToDrop();
            final int newExp = originalExp * 2;

            event.setExpToDrop(newExp);

            return;
        }

        if (addon.getActiveBoost() == null || !addon.getActiveBoost().getBoost().getType().equals(Boost.BoostType.ORES)) {
            return;
        }

        if (
                        !block.getType().equals(Material.COAL_ORE) &&
                        !block.getType().equals(Material.IRON_ORE) &&
                        !block.getType().equals(Material.GOLD_ORE) &&
                        !block.getType().equals(Material.REDSTONE_ORE) &&
                        !block.getType().equals(Material.GLOWING_REDSTONE_ORE) &&
                        !block.getType().equals(Material.LAPIS_ORE) &&
                        !block.getType().equals(Material.DIAMOND_ORE) &&
                        !block.getType().equals(Material.EMERALD_ORE)) {

            return;

        }

        final ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        final List<ItemStack> originalDrops = Lists.newArrayList(block.getDrops(hand));
        final List<ItemStack> newDrops = Lists.newArrayList();

        originalDrops.forEach(drop -> {
            drop.setAmount(drop.getAmount() * 2);
            newDrops.add(drop);
        });

        event.getBlock().getDrops().clear();
        event.getBlock().getDrops().addAll(newDrops);
    }
}
