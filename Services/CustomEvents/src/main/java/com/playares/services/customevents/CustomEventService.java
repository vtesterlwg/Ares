package com.playares.services.customevents;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.event.*;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Items;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public final class CustomEventService implements AresService, Listener {
    @Getter
    public final AresPlugin owner;

    public CustomEventService(AresPlugin owner) {
        this.owner = owner;
    }

    public void start() {
        registerListener(this);
    }

    public void stop() {
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

    public String getName() {
        return "Custom Events";
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final ProcessedChatEvent customEvent = new ProcessedChatEvent(event.getPlayer(), event.getMessage(), event.getRecipients());

        Bukkit.getPluginManager().callEvent(customEvent);

        event.setCancelled(true);

        if (customEvent.isCancelled()) {
            return;
        }

        customEvent.getRecipients().forEach(viewer -> {
            if (viewer != null && viewer.isOnline()) {
                viewer.sendMessage(customEvent.getDisplayName() + ": " + customEvent.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        final PlayerBigMoveEvent customEvent = new PlayerBigMoveEvent(player, from, to);
        Bukkit.getPluginManager().callEvent(customEvent);

        if (customEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player damaged = (Player)event.getEntity();
        Player damager = null;
        PlayerDamagePlayerEvent.DamageType type = null;

        if (event.getDamager() instanceof Player) {
            damager = (Player)event.getDamager();
            type = PlayerDamagePlayerEvent.DamageType.PHYSICAL;
        }

        else if (event.getDamager() instanceof Projectile) {
            final Projectile projectile = (Projectile)event.getDamager();
            final ProjectileSource source = projectile.getShooter();

            if (!(source instanceof Player)) {
                return;
            }

            damager = (Player)source;
            type = PlayerDamagePlayerEvent.DamageType.PROJECTILE;
        }

        if (damager == null) {
            return;
        }

        final PlayerDamagePlayerEvent customEvent = new PlayerDamagePlayerEvent(damager, damaged, type, event.getDamage());
        Bukkit.getPluginManager().callEvent(customEvent);

        if (customEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        final ThrownPotion potion = event.getPotion();

        if (!(potion.getShooter() instanceof Player)) {
            return;
        }

        if (event.getAffectedEntities().isEmpty()) {
            return;
        }

        final Player player = (Player)potion.getShooter();

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player)) {
                continue;
            }

            final Player affected = (Player)entity;
            final PlayerSplashPlayerEvent customEvent = new PlayerSplashPlayerEvent(player, affected, potion);

            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onLingeringSplash(AreaEffectCloudApplyEvent event) {
        final AreaEffectCloud cloud = event.getEntity();
        final ProjectileSource source = cloud.getSource();
        final List<Player> toRemove = Lists.newArrayList();

        if (!(source instanceof Player)) {
            return;
        }

        if (event.getAffectedEntities().isEmpty()) {
            return;
        }

        final Player damager = (Player)source;

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player)) {
                continue;
            }

            final Player damaged = (Player)entity;

            final PlayerLingeringSplashPlayerEvent customEvent = new PlayerLingeringSplashPlayerEvent(damager, damaged, cloud);
            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                toRemove.add(damaged);
            }
        }

        event.getAffectedEntities().removeAll(toRemove);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        final Player player = (Player)event.getWhoClicked();
        final InventoryAction action = event.getAction();
        final InventoryType.SlotType type = event.getSlotType();
        final ItemStack item = event.getCurrentItem();
        final ItemStack cursor = event.getCursor();
        final ItemStack toCheck = (cursor != null && !cursor.getType().equals(Material.AIR)) ? cursor : item;

        if (action.equals(InventoryAction.NOTHING)) {
            return;
        }

        if (event.getRawSlot() == 5 && !Items.isHelmet(toCheck)) {
            return;
        }

        if (event.getRawSlot() == 6 && !Items.isChestplate(toCheck)) {
            return;
        }

        if (event.getRawSlot() == 7 && !Items.isLeggings(toCheck)) {
            return;
        }

        if (event.getRawSlot() == 8 && !Items.isBoots(toCheck)) {
            return;
        }

        if (type.equals(InventoryType.SlotType.ARMOR)) {
            final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, event.getCurrentItem());

            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                event.setCancelled(true);
            }

            return;
        }

        if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            if (type.equals(InventoryType.SlotType.ARMOR) || type.equals(InventoryType.SlotType.QUICKBAR) || type.equals(InventoryType.SlotType.CONTAINER)) {
                final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, event.getCurrentItem());

                Bukkit.getPluginManager().callEvent(customEvent);

                if (customEvent.isCancelled()) {
                    event.setCancelled(true);
                }

                return;
            }
        }

        if (action.equals(InventoryAction.HOTBAR_SWAP)) {
            if (type.equals(InventoryType.SlotType.ARMOR)) {
                final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, event.getCurrentItem());

                Bukkit.getPluginManager().callEvent(customEvent);

                if (customEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getWhoClicked();
        final ItemStack item = event.getOldCursor();

        if (item == null) {
            return;
        }

        if (Items.isHelmet(item)) {
            if (event.getRawSlots().contains(5)) {
                final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);

                Bukkit.getPluginManager().callEvent(customEvent);

                if (customEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }

            return;
        }

        if (Items.isChestplate(item)) {
            if (event.getRawSlots().contains(6)) {
                final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);

                Bukkit.getPluginManager().callEvent(customEvent);

                if (customEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }

            return;
        }

        if (Items.isLeggings(item)) {
            if (event.getRawSlots().contains(7)) {
                final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);

                Bukkit.getPluginManager().callEvent(customEvent);

                if (customEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }

            return;
        }

        if (Items.isBoots(item)) {
            if (event.getRawSlots().contains(8)) {
                final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);

                Bukkit.getPluginManager().callEvent(customEvent);

                if (customEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        if (item == null || !Items.isArmor(item)) {
            return;
        }

        if (event.getClickedBlock() != null && Items.isInteractable(event.getClickedBlock().getType())) {
            return;
        }

        if (Items.isHelmet(item) && (player.getInventory().getHelmet() == null || player.getInventory().getHelmet().getType().equals(Material.AIR))) {
            final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);

            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                event.setCancelled(true);
            }

            return;
        }

        if (Items.isChestplate(item) && (player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType().equals(Material.AIR))) {
            final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);

            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                event.setCancelled(true);
            }

            return;
        }

        if (Items.isLeggings(item) && (player.getInventory().getLeggings() == null || player.getInventory().getLeggings().getType().equals(Material.AIR))) {
            final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);

            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                event.setCancelled(true);
            }

            return;
        }

        if (Items.isBoots(item) && (player.getInventory().getBoots() == null || player.getInventory().getBoots().getType().equals(Material.AIR))) {
            final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);

            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                event.setCancelled(true);
            }

            return;
        }
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getBrokenItem();

        if (item == null || !Items.isArmor(item)) {
            return;
        }

        final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, item);
        Bukkit.getPluginManager().callEvent(customEvent);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType().equals(Material.AIR)) {
                continue;
            }

            final PlayerArmorEvent customEvent = new PlayerArmorEvent(player, armor);
            Bukkit.getPluginManager().callEvent(customEvent);
        }
    }
}