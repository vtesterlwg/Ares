package com.riotmc.services.humbug.features.cont;

import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public final class AntiGrief implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public boolean fireSpreadDisabled;
    @Getter @Setter public boolean entityGriefDisabled;
    @Getter @Setter public boolean cobblestoneGeneratorDisabled;
    @Getter @Setter public boolean destroyingMobSpawnsDisabled;
    @Getter @Setter public boolean spawnNetherPortalPlatform;
    @Getter @Setter public boolean pushableLiquidsDisabled;

    public AntiGrief(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("anti-grief.enabled");
        this.fireSpreadDisabled = humbug.getHumbugConfig().getBoolean("anti-grief.disable-fire-spread");
        this.entityGriefDisabled = humbug.getHumbugConfig().getBoolean("anti-grief.disable-entity-grief");
        this.cobblestoneGeneratorDisabled = humbug.getHumbugConfig().getBoolean("anti-grief.disable-cobble-generators");
        this.destroyingMobSpawnsDisabled = humbug.getHumbugConfig().getBoolean("anti-grief.disable-mob-spawner-breaking");
        this.spawnNetherPortalPlatform = humbug.getHumbugConfig().getBoolean("anti-grief.spawn-nether-portal-platform");
        this.pushableLiquidsDisabled = humbug.getHumbugConfig().getBoolean("anti-grief.disable-pushable-liquid");
    }

    @Override
    public String getName() {
        return "Anti-Grief";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);
    }

    @Override
    public void stop() {}

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isEnabled() && isEntityGriefDisabled()) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isEnabled() && isEntityGriefDisabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        if (isEnabled() && event.getSource().getType().equals(Material.FIRE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        if (isEnabled() && isCobblestoneGeneratorDisabled() && event.getNewState().getType().equals(Material.COBBLESTONE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!isEnabled() || !isDestroyingMobSpawnsDisabled()) {
            return;
        }

        if (!block.getType().equals(Material.SPAWNER)) {
            return;
        }

        if (player.hasPermission("humbug.antigrief.bypass")) {
            return;
        }

        player.sendMessage(ChatColor.RED + "Spawners can not be broken");
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (!isEnabled() || !isSpawnNetherPortalPlatform() || event.isCancelled()) {
            return;
        }

        final Location to = event.getTo();

        if (!to.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            return;
        }

        new Scheduler(getHumbug().getOwner()).sync(() -> {
            final int minX = to.getBlockX() - 5;
            final int maxX = to.getBlockX() + 5;
            final int y = to.getBlockY() - 1;
            final int minZ = to.getBlockZ() - 5;
            final int maxZ = to.getBlockZ() + 5;

            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    final Block block = to.getWorld().getBlockAt(x, y, z);
                    block.setType(Material.OBSIDIAN);
                }
            }
        }).delay(1L).run();
    }

    @EventHandler
    public void onBlockMove(BlockPistonExtendEvent event) {
        if (event.isCancelled() || isPushableLiquidsDisabled()) {
            return;
        }

        for (Block block : event.getBlocks()) {
            if (!(block.getState().getData() instanceof Waterlogged)) {
                continue;
            }

            if (!block.getType().name().contains("_FENCE")) {
                continue;
            }

            event.setCancelled(true);
            return;
        }
    }
}