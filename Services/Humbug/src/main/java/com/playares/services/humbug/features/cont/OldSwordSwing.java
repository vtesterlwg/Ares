package com.playares.services.humbug.features.cont;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.commons.bukkit.util.Worlds;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class OldSwordSwing implements HumbugModule, Listener {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean enabled;

    @Getter @Setter
    public int hitDelayTicks;

    public OldSwordSwing(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("old-sword-swing.enabled");
        this.hitDelayTicks = humbug.getHumbugConfig().getInt("old-sword-swing.hit-delay-ticks");
    }

    @Override
    public String getName() {
        return "1.8 Sword Swinging";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);

        this.humbug.getOwner().getProtocol().addPacketListener(new PacketAdapter(this.humbug.getOwner(), ListenerPriority.LOWEST, PacketType.Play.Client.USE_ENTITY) {
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

                    new Scheduler(humbug.getOwner()).sync(() -> {
                        final LivingEntity damaged = (LivingEntity)entity;

                        if (damaged.isDead() || damaged.getNoDamageTicks() > 0) {
                            return;
                        }

                        double init = ((CraftPlayer)damager).getHandle().getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();

                        if (!damager.isOnGround() && damager.getVelocity().getY() < 0) {
                            init *= 1.5;
                            Worlds.spawnParticle(damaged.getLocation().add(0, 1.0, 0), Particle.CRIT, 8);
                        }

                        damaged.damage(init, damager);
                        damaged.setNoDamageTicks(hitDelayTicks);
                    }).run();
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
}