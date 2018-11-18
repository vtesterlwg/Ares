package com.riotmc.services.customitems;

import com.google.common.collect.Maps;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.item.custom.CustomBlock;
import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.commons.bukkit.item.custom.event.CustomBlockPlaceEvent;
import com.playares.commons.bukkit.item.custom.event.CustomItemInteractEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.RiotService;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

public final class CustomItemService implements RiotService, Listener {
    @Getter
    public final RiotPlugin owner;

    @Getter
    public final Map<Class<? extends CustomItem>, CustomItem> registry;

    public CustomItemService(RiotPlugin owner) {
        this.owner = owner;
        this.registry = Maps.newConcurrentMap();
    }

    public void start() {
        registerListener(this);
    }

    public void stop() {
        InventoryMoveItemEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);

        registry.clear();
    }

    public String getName() {
        return "Custom Items";
    }

    public void registerNewItem(CustomItem item) {
        registry.put(item.getClass(), item);
        Logger.print("Registered custom item: " + item.getName() + ", Class: " + item.getClass().getSimpleName());
    }

    public Optional<CustomItem> getItem(ItemStack itemstack) {
        if (!itemstack.hasItemMeta() || itemstack.getItemMeta().getDisplayName() == null) {
            return Optional.empty();
        }

        return registry.values().stream().filter(item -> item.getName().equals(itemstack.getItemMeta().getDisplayName())).findFirst();
    }

    public Optional<CustomItem> getItem(Class<? extends CustomItem> clazz) {
        return Optional.ofNullable(registry.get(clazz));
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItemDrop().getItemStack();

        if (event.isCancelled()) {
            return;
        }

        getItem(item).ifPresent(customItem -> {
            if (customItem.isSoulbound()) {
                Players.spawnParticle(player, player.getEyeLocation(), Particle.EXPLOSION_NORMAL, 1);
                Players.playSound(player, Sound.ENTITY_ITEM_BREAK);
                event.getItemDrop().remove();
                player.sendMessage(ChatColor.RED + "Soulbound item has been removed from your inventory");
            }
        });
    }

    @EventHandler
    public void onPlayerRepairAttempt(InventoryMoveItemEvent event) {
        final ItemStack item = event.getItem();
        final Inventory inventory = event.getDestination();

        if (!(inventory instanceof AnvilInventory)) {
            return;
        }

        getItem(item).ifPresent(customItem -> {
            if (!customItem.isRepairable()) {
                event.setCancelled(true);

                if (event.getSource().getHolder() instanceof Player) {
                    final Player player = (Player)event.getSource().getHolder();
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "This item can not be repaired");
                }
            }
        });
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (event.getAction().equals(Action.PHYSICAL) || /* event.isCancelled() || */ item == null) {
            return;
        }

        getItem(item).ifPresent(customItem -> {
            final CustomItemInteractEvent customEvent = new CustomItemInteractEvent(player, customItem, event.getAction(), event.getClickedBlock());
            Bukkit.getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                return;
            }

            if (customEvent.isLeftClick() && customItem.getLeftClick(player) != null) {
                new Scheduler(getOwner()).sync(customItem.getLeftClick(player)).run();
                return;
            }

            if (customEvent.isRightClick() && customItem.getRightClick(player) != null) {
                new Scheduler(getOwner()).sync(customItem.getRightClick(player)).run();
            }
        });
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final ItemStack itemstack = event.getItemInHand();

        getItem(itemstack).ifPresent(customItem -> {
            if (customItem instanceof CustomBlock) {
                final CustomBlock customBlock = (CustomBlock)customItem;
                final CustomBlockPlaceEvent customEvent = new CustomBlockPlaceEvent(player, customBlock);
                Bukkit.getPluginManager().callEvent(customEvent);

                if (customEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }

                if (!customBlock.isPlaceable()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This block can not be placed");
                    return;
                }

                if (customBlock.getPlace(player) != null) {
                    new Scheduler(getOwner()).sync(customBlock.getPlace(player)).run();
                }
            }
        });
    }
}