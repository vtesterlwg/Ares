package com.playares.factions.addons.loggers.data;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public final class CombatLogger extends EntityVillager {
    /** Player UUID that this logger entity belongs to **/
    @Getter @Setter public UUID owner;
    /** Player Username that this logger entity belongs to **/
    @Getter @Setter public String ownerUsername;
    /** Items this logger is storing **/
    @Getter @Setter public List<ItemStack> items;

    /**
     * Build an empty combat logger to prevent an init error
     * @param world Bukkit World
     */
    public CombatLogger(World world) {
        super(world);
        this.owner = null;
        this.ownerUsername = null;
        this.items = Lists.newArrayList();
    }

    /**
     * Build a combat logger for a specified player
     * @param world Bukkit World
     * @param player Bukkit Player
     */
    public CombatLogger(World world, Player player) {
        super(world);

        this.owner = player.getUniqueId();
        this.ownerUsername = player.getName();
        this.items = Lists.newArrayList();
        this.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(ChatColor.RED + "(Combat Logger) " + ChatColor.RESET + player.getName());

        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            this.items.add(item);
        }

        final CraftLivingEntity living = (CraftLivingEntity)getBukkitEntity();

        living.setHealth(player.getHealth());
        living.setFallDistance(player.getFallDistance());
        living.setNoDamageTicks(player.getNoDamageTicks());
        living.setFireTicks(player.getFireTicks());
        living.setRemainingAir(player.getRemainingAir());
        living.setCollidable(false);

        if (living.getEquipment() != null && player.getEquipment() != null) {
            living.getEquipment().setHelmet(player.getEquipment().getHelmet());
            living.getEquipment().setChestplate(player.getEquipment().getChestplate());
            living.getEquipment().setLeggings(player.getEquipment().getLeggings());
            living.getEquipment().setBoots(player.getEquipment().getBoots());
        }

        player.getActivePotionEffects().forEach(living::addPotionEffect);
    }

    @Override
    public void move(EnumMoveType enummovetype, double d0, double d1, double d2) {
        super.move(enummovetype, 0.0, d1, 0.0);

        if (this.motY > 0.0) {
            this.motY = 0.0;
        }
    }

    public void reapply(Player player) {
        final CraftLivingEntity living = (CraftLivingEntity)getBukkitEntity();

        if (player.getEquipment() != null && living.getEquipment() != null) {
            player.getEquipment().setHelmet(living.getEquipment().getHelmet());
            player.getEquipment().setChestplate(living.getEquipment().getChestplate());
            player.getEquipment().setLeggings(living.getEquipment().getLeggings());
            player.getEquipment().setBoots(living.getEquipment().getBoots());
        }

        player.setHealth(living.getHealth());
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.teleport(living.getLocation());
        player.setFallDistance(living.getFallDistance());
        player.setFireTicks(living.getFireTicks());
        player.setRemainingAir(living.getRemainingAir());

        living.getActivePotionEffects().forEach(player::addPotionEffect);
        living.remove();
    }

    public void spawn() {
        world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public void dropItems(Location location) {
        for (ItemStack item : items) {
            location.getWorld().dropItem(location, item);
        }
    }
}