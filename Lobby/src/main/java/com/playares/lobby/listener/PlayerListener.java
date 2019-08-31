package com.playares.lobby.listener;

import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.util.Players;
import com.playares.lobby.Lobby;
import com.playares.lobby.util.LobbyUtils;
import com.playares.services.ranks.RankService;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public final class PlayerListener implements Listener {
    @Getter public final Lobby plugin;

    public PlayerListener(Lobby plugin) {
        this.plugin = plugin;
    }

    private void checkPermissions(Player player, Cancellable event) {
        if (player.hasPermission("lobby.edit")) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        final RankService rankService = (RankService)getPlugin().getService(RankService.class);

        event.setJoinMessage(null);

        Players.resetWalkSpeed(player);
        Players.resetHealth(player);

        player.setWalkSpeed(0.4F);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.SURVIVAL);

        Players.sendTablist(getPlugin().getProtocol(), player, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Ares Network", ChatColor.AQUA + "playares.net");
        LobbyUtils.giveStandardItems(plugin, player);

        if (rankService != null && player.hasPermission("lobby.premium")) {
            Bukkit.broadcastMessage(rankService.formatName(player) + ChatColor.YELLOW + " has joined the lobby");
        }

        if (player.hasPermission("lobby.premium") || player.hasPermission("lobby.staff")) {
            LobbyUtils.givePremiumItems(player);
        }

        getPlugin().getSpawnManager().teleport(player);
        getPlugin().getNameplateManager().apply(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        getPlugin().getNameplateManager().remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final Block floorBlock = event.getTo().getBlock();

        if (floorBlock == null || !floorBlock.getType().name().contains("_PLATE")) {
            return;
        }

        final Vector velocity = player.getLocation().getDirection();

        if (floorBlock.getType().equals(Material.WOOD_PLATE)) {
            velocity.setY(velocity.getY() + 0.3);
            velocity.multiply(2.25);
        }

        if (floorBlock.getType().equals(Material.STONE_PLATE)) {
            velocity.setY(velocity.getY() + 0.3);
            velocity.multiply(2.50);
        }

        if (floorBlock.getType().equals(Material.IRON_PLATE)) {
            velocity.setY(velocity.getY() + 0.3);
            velocity.multiply(2.75);
        }

        if (floorBlock.getType().equals(Material.GOLD_PLATE)) {
            velocity.setY(velocity.getY() + 0.3);
            velocity.multiply(3.00);
        }

        player.setVelocity(velocity);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(PlayerDamagePlayerEvent event) {
        final Player damager = event.getDamager();
        final Player damaged = event.getDamaged();

        event.setCancelled(true);

        if (damaged.hasPermission("lobby.staff")) {
            return;
        }

        damager.spawnParticle(Particle.EXPLOSION_NORMAL, damaged.getLocation().add(0, 1.0, 0), 8, 0.1, 0.1, 0.1, 0.5);
        Players.playSound(damager, Sound.ENTITY_ITEM_PICKUP);

        damager.hidePlayer(getPlugin(), damaged);
        damager.sendMessage(ChatColor.AQUA + "Pop!");
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        event.setFoodLevel(10);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        checkPermissions(event.getPlayer(), event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        checkPermissions(event.getPlayer(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        checkPermissions(event.getPlayer(), event);
    }
}