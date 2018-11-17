package com.playares.services.customevents;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.event.*;
import com.playares.commons.bukkit.service.RiotService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public final class CustomEventService implements RiotService, Listener {
    @Getter
    public final RiotPlugin owner;

    public CustomEventService(RiotPlugin owner) {
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
}