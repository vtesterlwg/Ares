package com.playares.factions.addons.loggers;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityVillager;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public final class CombatLogger extends EntityVillager {
    @Getter @Setter
    public UUID owner;

    @Getter @Setter
    public String ownerUsername;

    @Getter @Setter
    public List<ItemStack> items;

    public CombatLogger(World world) {
        super(world);
    }

    public CombatLogger(World world, Player player) {
        super(world);
        this.owner = player.getUniqueId();
        this.ownerUsername = player.getName();
        this.items = Lists.newArrayList();
        this.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(ChatColor.RED + "(Combat Logger) " + ChatColor.RESET + player.getName());

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            this.items.add(item);
        }

        getBukkitLivingEntity().setHealth(player.getHealth());
        getBukkitLivingEntity().setFallDistance(player.getFallDistance());
        getBukkitLivingEntity().setNoDamageTicks(player.getNoDamageTicks());
        getBukkitLivingEntity().setFireTicks(player.getFireTicks());
        getBukkitLivingEntity().setRemainingAir(player.getRemainingAir());
        getBukkitLivingEntity().setCollidable(false);

        getBukkitLivingEntity().getEquipment().setHelmet(player.getEquipment().getHelmet());
        getBukkitLivingEntity().getEquipment().setChestplate(player.getEquipment().getChestplate());
        getBukkitLivingEntity().getEquipment().setLeggings(player.getEquipment().getLeggings());
        getBukkitLivingEntity().getEquipment().setBoots(player.getEquipment().getBoots());

        player.getActivePotionEffects().forEach(effect -> getBukkitLivingEntity().addPotionEffect(effect));
    }

    @Override
    public void a(Entity entity, float f, double d0, double d1) {}

    @Override
    protected void n() {}

    public void reapply(Player player) {
        player.getEquipment().setHelmet(getBukkitLivingEntity().getEquipment().getHelmet());
        player.getEquipment().setChestplate(getBukkitLivingEntity().getEquipment().getChestplate());
        player.getEquipment().setLeggings(getBukkitLivingEntity().getEquipment().getLeggings());
        player.getEquipment().setBoots(getBukkitLivingEntity().getEquipment().getBoots());
        player.setHealth(getBukkitLivingEntity().getHealth());
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.teleport(getBukkitLivingEntity().getLocation());
        player.setFallDistance(getBukkitLivingEntity().getFallDistance());
        player.setFireTicks(getBukkitLivingEntity().getFireTicks());
        player.setRemainingAir(getBukkitLivingEntity().getRemainingAir());
        getBukkitLivingEntity().getActivePotionEffects().forEach(player::addPotionEffect);
        getBukkitLivingEntity().remove();
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