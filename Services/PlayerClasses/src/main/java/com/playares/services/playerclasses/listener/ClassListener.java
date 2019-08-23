package com.playares.services.playerclasses.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.collect.Sets;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.data.Class;
import com.playares.services.playerclasses.data.ClassConsumable;
import com.playares.services.playerclasses.data.cont.ArcherClass;
import com.playares.services.playerclasses.event.ConsumeClassItemEvent;
import com.playares.services.playerclasses.event.PlayerClassDeactivateEvent;
import com.playares.services.playerclasses.event.PlayerClassReadyEvent;
import com.playares.services.playerclasses.event.PlayerClassUnreadyEvent;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public final class ClassListener implements Listener {
    @Getter public final PlayerClassService service;
    @Getter public final Set<UUID> recentlyLoggedIn;

    public ClassListener(PlayerClassService service) {
        this.service = service;
        this.recentlyLoggedIn = Sets.newConcurrentHashSet();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();

        player.getActivePotionEffects().stream().filter(effect -> effect.getDuration() > 25000).forEach(infiniteEffect -> player.removePotionEffect(infiniteEffect.getType()));
        recentlyLoggedIn.add(player.getUniqueId());

        new Scheduler(service.getOwner()).sync(() -> {
            recentlyLoggedIn.remove(uniqueId);

            final Class playerClass = service.getClassManager().getClassByArmor(player);

            if (playerClass != null) {
                final PlayerClassReadyEvent readyEvent = new PlayerClassReadyEvent(player, playerClass);
                Bukkit.getPluginManager().callEvent(readyEvent);
            }
        }).delay(3L).run();
    }

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (event.getOldItem() != null && event.getNewItem() != null && event.getOldItem().getType().equals(event.getNewItem().getType())) {
            return;
        }

        final Player player = event.getPlayer();

        if (recentlyLoggedIn.contains(player.getUniqueId())) {
            return;
        }

        final Class actualClass = service.getClassManager().getCurrentClass(player);
        final Class expectedClass = service.getClassManager().getClassByArmor(player);

        if (expectedClass != null) {
            if (actualClass != null) {
                actualClass.deactivate(player);
            }

            final PlayerClassReadyEvent readyEvent = new PlayerClassReadyEvent(player, expectedClass);
            Bukkit.getPluginManager().callEvent(readyEvent);

            return;
        }

        if (actualClass != null) {
            final PlayerClassDeactivateEvent deactivateEvent = new PlayerClassDeactivateEvent(player, actualClass);
            Bukkit.getPluginManager().callEvent(deactivateEvent);
            actualClass.deactivate(player, actualClass.getActivePlayers().contains(player.getUniqueId()));
        } else {
            final PlayerClassUnreadyEvent unreadyEvent = new PlayerClassUnreadyEvent(player);
            Bukkit.getPluginManager().callEvent(unreadyEvent);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final Entity damaged = event.getEntity();
        double damage = event.getFinalDamage();

        if (event.isCancelled()) {
            return;
        }

        if (!(damager instanceof Arrow)) {
            return;
        }

        if (!(damaged instanceof LivingEntity)) {
            return;
        }

        final Projectile arrow = (Projectile)damager;

        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)arrow.getShooter();
        final Class playerClass = getService().getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof ArcherClass)) {
            return;
        }

        final ArcherClass archerClass = (ArcherClass)playerClass;
        final double maxDamage = archerClass.getMaxDealtDamage();
        final double damagePerBlock = archerClass.getDamagePerBlock();
        final Location locA = player.getLocation().clone();
        final Location locB = damaged.getLocation().clone();

        locA.setY(64.0);
        locB.setY(64.0);

        final double distance = locA.distance(locB);
        final double finalDamage = (((damagePerBlock * distance) + damage) > maxDamage) ? maxDamage : (damagePerBlock * distance) + damage;

        event.setDamage(finalDamage);

        player.sendMessage(ChatColor.YELLOW + "[" + ChatColor.BLUE + "Archer" + ChatColor.YELLOW + " w/ " + ChatColor.BLUE + "Range" + ChatColor.YELLOW +
                "(" + ChatColor.RED + String.format("%.2f", distance) + ChatColor.YELLOW + ")]: Damage Increase (" + ChatColor.RED + String.format("%.2f", damage) + ChatColor.YELLOW + " => " +
                ChatColor.BLUE + String.format("%.2f", finalDamage) + ChatColor.YELLOW + ")");
    }

    @EventHandler
    public void onConsume(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack hand = player.getInventory().getItem(event.getHand());
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        // Prevents ASSERTION ERROR: TRAP
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        final Class playerClass = service.getClassManager().getCurrentClass(player);

        if (hand == null || hand.getType().equals(Material.AIR)) {
            return;
        }

        if (playerClass == null || playerClass.getConsumables().isEmpty()) {
            return;
        }

        final ClassConsumable consumable = playerClass.getConsumableByMaterial(hand.getType());

        if (consumable == null) {
            return;
        }

        // Prevents the physical item from being used
        if (consumable.getMaterial().equals(Material.EYE_OF_ENDER)) {
            event.setCancelled(true);
        }

        if (consumable.hasCooldown(player)) {
            player.sendMessage(ChatColor.RED + WordUtils.capitalize(consumable.getEffectType().getName().toLowerCase().replace("_", " ")) + " are locked for " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(consumable.getPlayerCooldown(player) - Time.now()) + ChatColor.RED + "s");

            return;
        }

        final ConsumeClassItemEvent consumeClassItemEvent = new ConsumeClassItemEvent(player, consumable);
        Bukkit.getPluginManager().callEvent(consumeClassItemEvent);

        if (consumeClassItemEvent.isCancelled()) {
            return;
        }

        consumable.consume(player, event.getHand());
    }
}