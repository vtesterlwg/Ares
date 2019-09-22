package com.playares.services.humbug.features.cont;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import com.google.common.collect.Lists;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Knockback implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public double horizontal;
    @Getter @Setter public double vertical;
    @Getter public final List<UUID> recentSprinters;
    @Getter @Setter public boolean enabled;

    public Knockback(HumbugService humbug) {
        this.humbug = humbug;
        this.recentSprinters = Collections.synchronizedList(Lists.newArrayList());
    }

    @Override
    public String getName() {
        return "Knockback";
    }

    @Override
    public void loadValues() {
        final YamlConfiguration config = getHumbug().getOwner().getConfig("humbug");

        this.enabled = config.getBoolean("knockback.enabled");
        this.horizontal = config.getDouble("knockback.horizontal-multiplier");
        this.vertical = config.getDouble("knockback.vertical-multiplier");
    }

    @Override
    public void start() {
        getHumbug().registerListener(this);
    }

    @Override
    public void stop() {
        recentSprinters.clear();

        PlayerVelocityEvent.getHandlerList().unregister(this);
        PlayerDamagePlayerEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    public boolean isRecentSprinter(Player player) {
        return recentSprinters.contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        final Player player = event.getPlayer();

        if (event.isSprinting()) {
            if (!isRecentSprinter(player)) {
                recentSprinters.add(player.getUniqueId());
            }

            return;
        }

        recentSprinters.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (isRecentSprinter(player)) {
            recentSprinters.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if (!isEnabled() || event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final EntityDamageEvent lastDamage = player.getLastDamageCause();

        if (!(lastDamage instanceof EntityDamageByEntityEvent)) {
            return;
        }

        final EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent)lastDamage;

        if (entityDamageEvent.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        if (!isEnabled() || event.isCancelled()) {
            return;
        }

        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();

        if (damaged.getNoDamageTicks() > damaged.getMaximumNoDamageTicks() / 2D) {
            return;
        }

        final double sprintMultiplier = (damager.isSprinting() ? (isRecentSprinter(damager) ? 0.8D : 0.65D) : 0.5D);
        final double enchantMultiplier = (damager.getInventory().getItemInMainHand() == null) ? 0 : damager.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.KNOCKBACK);
        final double airMultiplier = damaged.isOnGround() ? 1 : 0.5;

        final Vector velocity = damaged.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();

        velocity.setX((velocity.getX() * sprintMultiplier + enchantMultiplier) * horizontal);
        velocity.setY(0.35D * airMultiplier * vertical);
        velocity.setZ((velocity.getZ() * sprintMultiplier + enchantMultiplier) * horizontal);

        final PacketConstructor constructor = getHumbug().getOwner().getProtocol().createPacketConstructor(PacketType.Play.Server.ENTITY_VELOCITY, 0, (double)0, (double)0, (double)0);
        final PacketContainer container = constructor.createPacket(damaged.getEntityId(), velocity.getX(), velocity.getY(), velocity.getZ());

        try {
            getHumbug().getOwner().getProtocol().sendServerPacket(damaged, container);
        } catch (InvocationTargetException e) {
            Logger.error("Failed to send velocity packet", e);
        } finally {
            if (isRecentSprinter(damager)) {
                recentSprinters.remove(damager.getUniqueId());
            }
        }
    }
}