package com.playares.services.humbug.features.cont;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.Set;
import java.util.UUID;

public final class OldRegen implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter public final Set<UUID> recentHeals;

    public OldRegen(HumbugService humbug) {
        this.humbug = humbug;
        this.recentHeals = Sets.newConcurrentHashSet();
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("old-regen.enabled");
    }

    @Override
    public String getName() {
        return "Old Regen";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);
    }

    @Override
    public void stop() {
        EntityRegainHealthEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!isEnabled()) {
            return;
        }

        final Entity entity = event.getEntity();
        final EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();

        if (!(entity instanceof Player) || !reason.equals(EntityRegainHealthEvent.RegainReason.SATIATED)) {
            return;
        }

        event.setCancelled(true);

        final Player player = (Player)entity;
        final UUID uniqueId = player.getUniqueId();

        if (recentHeals.contains(uniqueId)) {
            return;
        }

        if (player.getHealth() >= player.getMaxHealth()) {
            return;
        }

        final float exhaustion = player.getExhaustion();
        new Scheduler(getHumbug().getOwner()).sync(() -> player.setExhaustion(exhaustion + 3)).delay(1L).run();

        final double health = player.getHealth();
        final double newHealth = health + 0.5;
        player.setHealth((newHealth > player.getMaxHealth()) ? player.getMaxHealth() : newHealth);
        recentHeals.add(player.getUniqueId());
        new Scheduler(getHumbug().getOwner()).sync(() -> recentHeals.remove(uniqueId)).delay(60L).run();
    }
}
