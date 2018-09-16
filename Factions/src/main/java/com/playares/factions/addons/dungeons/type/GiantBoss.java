package com.playares.factions.addons.dungeons.type;

import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.commons.bukkit.util.Worlds;
import com.playares.factions.Factions;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityGiantZombie;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

public final class GiantBoss extends EntityGiantZombie {
    private final Factions plugin;

    @Getter @Setter
    public long nextAllowedJump;

    @Getter @Setter
    public long nextAllowedKick;

    public GiantBoss(World world) {
        super(world);
        this.plugin = null;
        this.nextAllowedJump = Time.now();
        this.nextAllowedKick = Time.now();
    }

    public GiantBoss(World world, Factions plugin) {
        super(world);
        this.plugin = plugin;
        this.nextAllowedJump = Time.now();
        this.nextAllowedKick = Time.now();
    }

    public void spawn() {
        world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() == null || !(damagesource.getEntity() instanceof Player)) {
            return super.damageEntity(damagesource, f);
        }

        if (nextAllowedJump <= Time.now()) {
            jump();
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    public void a(Entity entity, float f, float f1) {
        super.a(entity, f, f1);

        if (nextAllowedKick <= Time.now()) {
            kick();
        }
    }

    public void jump() {
        if (plugin == null) {
            return;
        }

        Worlds.playSound(getBukkitLivingEntity().getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL);

        new Scheduler(plugin).sync(() -> {
            this.motY += 2.0;
            Worlds.playSound(getBukkitLivingEntity().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP);

            new Scheduler(plugin).sync(() -> {
                playGroundVibrate((int)(Math.round(locX)), (int)(Math.round(locY)), (int)(Math.round(locZ)));

                for (Player nearby : getBukkitLivingEntity().getWorld().getNearbyPlayers(getBukkitLivingEntity().getLocation(), 15)) {
                    if (nearby.getGameMode().equals(GameMode.CREATIVE) || nearby.getGameMode().equals(GameMode.SPECTATOR)) {
                        continue;
                    }

                    final double distance = nearby.getLocation().distance(getBukkitLivingEntity().getLocation());

                    // TODO: Apply damage and knock back
                }
            }).delay(10L).run();
        }).delay(10L).run();

        this.nextAllowedJump = Time.now() + (10 * 1000L);
    }

    public void kick() {
        this.nextAllowedKick = Time.now() + (3 * 1000L);
    }

    private void playGroundVibrate(int x, int y, int z) {
        if (plugin == null) {
            return;
        }

        Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), x, y, z), Particle.EXPLOSION_HUGE, 1);
        Worlds.playSound(new Location(getBukkitEntity().getWorld(), x, y, z), Sound.ENTITY_GENERIC_EXPLODE);

        new Scheduler(plugin).sync(() -> {
            Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 5), y, z), Particle.EXPLOSION_LARGE, 1);
            Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 5), y, z), Particle.EXPLOSION_LARGE, 1);
            Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), x, y, (z + 5)), Particle.EXPLOSION_LARGE, 1);
            Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), x, y, (z - 5)), Particle.EXPLOSION_LARGE, 1);
            Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 5), y, (z + 5)), Particle.EXPLOSION_LARGE, 1);
            Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 5), y, (z - 5)), Particle.EXPLOSION_LARGE, 1);
            Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 5), y, (z - 5)), Particle.EXPLOSION_LARGE, 1);
            Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 5), y, (z + 5)), Particle.EXPLOSION_LARGE, 1);

            Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x + 5), y, z), Sound.ENTITY_GENERIC_EXPLODE);
            Worlds.playSound(new Location(getBukkitEntity().getWorld(), x, y, (z + 5)), Sound.ENTITY_GENERIC_EXPLODE);
            Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x + 5), y, (z + 5)), Sound.ENTITY_GENERIC_EXPLODE);
            Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x - 5), y, (z - 5)), Sound.ENTITY_GENERIC_EXPLODE);

            new Scheduler(plugin).sync(() -> {
                Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 10), y, z), Particle.EXPLOSION_LARGE, 1);
                Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 10), y, z), Particle.EXPLOSION_LARGE, 1);
                Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), x, y, (z + 10)), Particle.EXPLOSION_LARGE, 1);
                Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), x, y, (z - 10)), Particle.EXPLOSION_LARGE, 1);
                Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 10), y, (z + 10)), Particle.EXPLOSION_LARGE, 1);
                Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 10), y, (z - 10)), Particle.EXPLOSION_LARGE, 1);
                Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 10), y, (z - 10)), Particle.EXPLOSION_LARGE, 1);
                Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 10), y, (z + 10)), Particle.EXPLOSION_LARGE, 1);

                Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x + 10), y, z), Sound.ENTITY_GENERIC_EXPLODE);
                Worlds.playSound(new Location(getBukkitEntity().getWorld(), x, y, (z + 10)), Sound.ENTITY_GENERIC_EXPLODE);
                Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x + 10), y, (z + 10)), Sound.ENTITY_GENERIC_EXPLODE);
                Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x - 10), y, (z - 10)), Sound.ENTITY_GENERIC_EXPLODE);

                new Scheduler(plugin).sync(() -> {
                    Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 15), y, z), Particle.EXPLOSION_NORMAL, 1);
                    Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 15), y, z), Particle.EXPLOSION_NORMAL, 1);
                    Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), x, y, (z + 15)), Particle.EXPLOSION_NORMAL, 1);
                    Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), x, y, (z - 15)), Particle.EXPLOSION_NORMAL, 1);
                    Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 15), y, (z + 15)), Particle.EXPLOSION_NORMAL, 1);
                    Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 15), y, (z - 15)), Particle.EXPLOSION_NORMAL, 1);
                    Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x + 15), y, (z - 15)), Particle.EXPLOSION_NORMAL, 1);
                    Worlds.spawnParticle(new Location(getBukkitEntity().getWorld(), (x - 15), y, (z + 15)), Particle.EXPLOSION_NORMAL, 1);

                    Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x + 15), y, z), Sound.ENTITY_GENERIC_EXPLODE);
                    Worlds.playSound(new Location(getBukkitEntity().getWorld(), x, y, (z + 15)), Sound.ENTITY_GENERIC_EXPLODE);
                    Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x + 15), y, (z + 15)), Sound.ENTITY_GENERIC_EXPLODE);
                    Worlds.playSound(new Location(getBukkitEntity().getWorld(), (x - 15), y, (z - 15)), Sound.ENTITY_GENERIC_EXPLODE);
                }).delay(5L).run();
            }).delay(5L).run();
        }).delay(5L).run();

    }
}
