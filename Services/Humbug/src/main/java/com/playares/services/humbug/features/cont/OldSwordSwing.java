package com.playares.services.humbug.features.cont;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public final class OldSwordSwing implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public int hitDelayTicks;
    @Getter @Setter public double maxReach;
    @Getter public BukkitTask queueProcessor;
    @Getter public final Set<UUID> attackCooldowns;
    @Getter private final Queue<QueuedAttack> attackQueue;

    public OldSwordSwing(HumbugService humbug) {
        this.humbug = humbug;
        this.attackQueue = Queues.newConcurrentLinkedQueue();
        this.attackCooldowns = Sets.newConcurrentHashSet();
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("old-sword-swing.enabled");
        this.hitDelayTicks = humbug.getHumbugConfig().getInt("old-sword-swing.hit-delay-ticks");
        this.maxReach = humbug.getHumbugConfig().getDouble("old-sword-swing.max-reach");
    }

    @Override
    public String getName() {
        return "1.8 Sword Swinging";
    }

    @Override
    public void start() {
        queueProcessor = new Scheduler(getHumbug().getOwner()).sync(() -> {
            while (attackQueue.size() > 0) {
                final QueuedAttack attack = attackQueue.remove();
                attack.getAttacked().damage(attack.getDamage(), attack.getAttacker());
                attack.getAttacked().setNoDamageTicks(hitDelayTicks);
            }
        }).repeat(0L, 1L).run();

        humbug.getOwner().registerListener(this);

        humbug.getOwner().getProtocol().addPacketListener(new PacketAdapter(humbug.getOwner(), ListenerPriority.LOWEST, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!isEnabled()) {
                    return;
                }

                final PacketContainer packet = event.getPacket();
                final Player damager = event.getPlayer();
                final Entity entity = event.getPacket().getEntityModifier(event).read(0);

                if (entity == null) {
                    return;
                }

                if (!packet.getEntityUseActions().read(0).equals(EnumWrappers.EntityUseAction.ATTACK)) {
                    return;
                }

                if (damager.getGameMode().equals(GameMode.SPECTATOR)) {
                    return;
                }

                if (entity instanceof LivingEntity) {
                    event.setCancelled(true);

                    final LivingEntity damaged = (LivingEntity)entity;
                    final UUID uniqueId = damaged.getUniqueId();
                    final double distance = damager.getLocation().distanceSquared(damaged.getLocation());

                    if (damaged.isDead()|| distance > (maxReach * maxReach)) {
                        return;
                    }

                    if (attackCooldowns.contains(uniqueId)) {
                        return;
                    }

                    double init = ((CraftPlayer)damager).getHandle().getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
                    boolean critical = false;

                    if (!damager.isOnGround() && damager.getVelocity().getY() < 0) {
                        init *= 1.25;
                        critical = true;

                        damaged.getLocation().getWorld().spawnParticle(Particle.CRIT, damaged.getLocation().add(0, 1.0, 0), 8, 0.1, 0.1, 0.1, 0.5);
                    }

                    attackQueue.add(new QueuedAttack(damager, damaged, init, critical));
                    attackCooldowns.add(uniqueId);

                    new Scheduler(getHumbug().getOwner()).async(() -> attackCooldowns.remove(uniqueId)).delay(hitDelayTicks).run();
                }
            }
        });
    }

    @Override
    public void stop() {
        attackQueue.clear();
        attackCooldowns.clear();

        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerChangedWorldEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024.0);
        player.saveData();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4.0);
        player.saveData();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024.0);
        player.saveData();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        final Player damager = event.getDamager();
        final Player damaged = event.getDamaged();
        final ItemStack hand = damager.getInventory().getItemInMainHand();

        if (hand != null && hand.hasItemMeta() && hand.getItemMeta().hasEnchant(Enchantment.FIRE_ASPECT)) {
            damaged.setFireTicks(80 * hand.getItemMeta().getEnchantLevel(Enchantment.FIRE_ASPECT));
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        final Entity damager = event.getDamager();
        final Entity entity = event.getEntity();

        if (!(damager instanceof Player) || entity instanceof Player) {
            return;
        }

        final Player player = (Player)damager;
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand != null && hand.hasItemMeta() && hand.getItemMeta().hasEnchant(Enchantment.FIRE_ASPECT)) {
            entity.setFireTicks(80 * hand.getItemMeta().getEnchantLevel(Enchantment.FIRE_ASPECT));
        }
    }

    @AllArgsConstructor
    public final class QueuedAttack {
        @Getter public final Player attacker;
        @Getter public final LivingEntity attacked;
        @Getter public final double damage;
        @Getter public final boolean critical;
    }
}