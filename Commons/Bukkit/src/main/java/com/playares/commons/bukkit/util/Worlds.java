package com.playares.commons.bukkit.util;

import com.google.common.base.Preconditions;
import com.playares.commons.bukkit.RiotPlugin;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Worlds {
    public static void playSound(Location location, Sound sound) {
        location.getWorld().playSound(location, sound, 1.0F, 1.0F);
    }

    public static void spawnEffect(Location location, Effect effect, int amount) {
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
        location.getWorld().playEffect(location, effect, amount);
    }

    public static void spawnParticle(Location location, Particle particle, int amount) {
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
        location.getWorld().spawnParticle(particle, location, amount);
    }

    public static void spawnParticle(Location location, Particle particle, int amount, double range) {
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
        location.getWorld().spawnParticle(particle, location, amount, range, range, range);
    }

    public static void spawnFakeLightning(Location location) {
        location.getWorld().strikeLightningEffect(location);
    }

    public static void spawnFirework(RiotPlugin plugin, Location location, int power, long detonate, FireworkEffect... effects) {
        final Firework firework = (Firework)location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        final FireworkMeta meta = firework.getFireworkMeta();

        meta.setPower(power);
        meta.addEffects(effects);

        firework.setFireworkMeta(meta);

        new Scheduler(plugin).sync(firework::detonate).delay(detonate).run();
    }

    private Worlds() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}
