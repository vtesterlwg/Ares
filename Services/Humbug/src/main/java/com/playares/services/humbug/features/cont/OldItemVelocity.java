package com.playares.services.humbug.features.cont;

import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public final class OldItemVelocity implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public double pearlVelocity;
    @Getter @Setter public double potionVelocity;

    public OldItemVelocity(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        final YamlConfiguration config = getHumbug().getOwner().getConfig("humbug");
        this.enabled = config.getBoolean("old-item-velocity.enabled");
        this.pearlVelocity = config.getDouble("old-item-velocity.pearl-velocity");
        this.potionVelocity = config.getDouble("old-item-velocity.potions");
    }

    @Override
    public String getName() {
        return "1.8 Item Velocity";
    }

    @Override
    public void start() {
        humbug.getOwner().registerListener(this);
    }

    @Override
    public void stop() {
        ProjectileLaunchEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!isEnabled()) {
            return;
        }

        final Projectile projectile = event.getEntity();

        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)projectile.getShooter();

        if (projectile instanceof EnderPearl) {
            projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(pearlVelocity));
            return;
        }

        if (projectile instanceof ThrownPotion) {
            projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(potionVelocity));
        }
    }
}