package com.playares.services.humbug.features.cont;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.Queues;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.commons.bukkit.util.Worlds;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Queue;

public final class OldSwordSwing implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public int hitDelayTicks;
    @Getter @Setter public double maxReach;
    @Getter public BukkitTask queueProcessor;
    @Getter private final Queue<QueuedAttack> attackQueue;

    public OldSwordSwing(HumbugService humbug) {
        this.humbug = humbug;
        this.attackQueue = Queues.newConcurrentLinkedQueue();
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

                final ItemStack attackerItem = attack.getAttacker().getInventory().getItemInMainHand();

                if (attackerItem != null && attackerItem.hasItemMeta() && attackerItem.getItemMeta().hasEnchant(Enchantment.FIRE_ASPECT)) {
                    attack.getAttacked().setFireTicks(80 * attackerItem.getItemMeta().getEnchantLevel(Enchantment.FIRE_ASPECT));
                }
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

                if (entity instanceof LivingEntity) {
                    event.setCancelled(true);

                    final LivingEntity damaged = (LivingEntity)entity;
                    final double distance = damager.getLocation().distanceSquared(damaged.getLocation());

                    if (damaged.isDead() || damaged.getNoDamageTicks() > 0 || distance > (maxReach * maxReach)) {
                        return;
                    }

                    double init = ((CraftPlayer)damager).getHandle().getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
                    boolean critical = false;

                    if (!damager.isOnGround() && damager.getVelocity().getY() < 0) {
                        init *= 1.25;
                        critical = true;

                        Worlds.spawnParticle(damaged.getLocation().add(0, 1.0, 0), Particle.CRIT, 10, -10);
                        Worlds.playSound(damaged.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT);
                    }

                    attackQueue.add(new QueuedAttack(damager, damaged, init, critical));
                }
            }
        });
    }

    @Override
    public void stop() {
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

    @AllArgsConstructor
    public final class QueuedAttack {
        @Getter public final Player attacker;
        @Getter public final LivingEntity attacked;
        @Getter public final double damage;
        @Getter public final boolean critical;
    }
}