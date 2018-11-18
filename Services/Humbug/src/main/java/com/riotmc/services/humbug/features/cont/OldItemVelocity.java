package com.riotmc.services.humbug.features.cont;

import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public final class OldItemVelocity implements HumbugModule, Listener {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean enabled;

    @Getter @Setter
    public double pearlVelocity;

    @Getter @Setter
    public double potionForwardVelocity;

    @Getter @Setter
    public double potionUpwardVelocity;

    public OldItemVelocity(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("old-item-velocity.enabled");
        this.pearlVelocity = humbug.getHumbugConfig().getDouble("old-item-velocity.pearl-velocity");
    }

    @Override
    public String getName() {
        return "1.8 Item Velocity";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);
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
            //projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(projectile.getVelocity().length()));
            projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(projectile.getVelocity().length()));
        }

        projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(projectile.getVelocity().length()));
    }
}