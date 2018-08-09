package com.playares.services.humbug.features.cont;

import com.google.common.collect.ImmutableMap;
import com.playares.commons.bukkit.util.Entities;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class OldItemValues implements HumbugModule, Listener {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean enabled;

    @Getter @Setter
    public ImmutableMap<Material, Double> weaponValues = ImmutableMap.<Material, Double>builder()
            .put(Material.WOODEN_AXE, 2.0).put(Material.STONE_AXE, 2.2).put(Material.IRON_AXE, 2.4).put(Material.GOLDEN_AXE, 2.4).put(Material.DIAMOND_AXE, 2.6)
            .put(Material.WOODEN_SHOVEL, 1.0).put(Material.STONE_SHOVEL, 1.0).put(Material.IRON_SHOVEL, 1.0).put(Material.GOLDEN_SHOVEL, 1.0).put(Material.DIAMOND_SHOVEL, 1.0)
            .put(Material.WOODEN_PICKAXE, 1.0).put(Material.STONE_PICKAXE, 1.0).put(Material.IRON_PICKAXE, 1.0).put(Material.GOLDEN_PICKAXE, 1.0).put(Material.DIAMOND_PICKAXE, 1.0)
            .put(Material.WOODEN_HOE, 1.0).put(Material.STONE_HOE, 1.0).put(Material.IRON_HOE, 1.0).put(Material.GOLDEN_HOE, 1.0).put(Material.DIAMOND_HOE, 1.0)
            .put(Material.WOODEN_SWORD, 2.0).put(Material.STONE_SWORD, 1.8).put(Material.IRON_SWORD, 1.6).put(Material.GOLDEN_SWORD, 1.6).put(Material.DIAMOND_SWORD, 1.4)
            .build();

    @Getter @Setter
    public ImmutableMap<Material, Double> armorValues = ImmutableMap.<Material, Double>builder()
            .put(Material.LEATHER_HELMET, 0.25).put(Material.LEATHER_CHESTPLATE, 0.75).put(Material.LEATHER_LEGGINGS, 0.50).put(Material.LEATHER_BOOTS, 0.25)
            .put(Material.CHAINMAIL_HELMET, 0.75).put(Material.CHAINMAIL_CHESTPLATE, 1.25).put(Material.CHAINMAIL_LEGGINGS, 1.0).put(Material.CHAINMAIL_BOOTS, 0.75)
            .put(Material.IRON_HELMET, 1.25).put(Material.IRON_CHESTPLATE, 1.75).put(Material.IRON_LEGGINGS, 1.5).put(Material.IRON_BOOTS, 1.25)
            .put(Material.GOLDEN_HELMET, 1.25).put(Material.GOLDEN_CHESTPLATE, 1.75).put(Material.GOLDEN_LEGGINGS, 1.5).put(Material.GOLDEN_BOOTS, 1.25)
            .put(Material.DIAMOND_HELMET, 2.0).put(Material.DIAMOND_CHESTPLATE, 2.5).put(Material.DIAMOND_LEGGINGS, 2.25).put(Material.DIAMOND_BOOTS, 2.0)
            .build();

    public OldItemValues(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("modules.old-item-values.enabled");
    }

    @Override
    public String getName() {
        return "1.8 Item Values";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);
    }

    @Override
    public void stop() {
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled() || event.isCancelled()) {
            return;
        }

        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getDamager();
        final Player damaged = (Player)event.getEntity();
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null) {
            return;
        }

        double init = event.getDamage();
        double enchantDamage = getDamageByEnchantment(event.getEntityType(), player.getItemInHand(), init);
        double divider = weaponValues.getOrDefault(hand.getType(), 1.0);
        double postDamage = getDamageByArmor(damaged.getInventory().getArmorContents(), ((init + enchantDamage) / divider));

        if (postDamage < 0) {
            postDamage = 0;
        }

        event.setDamage(postDamage);
    }

    private double getDamageByEnchantment(EntityType entity, ItemStack item, double init) {
        if (item == null || item.getType().equals(Material.AIR)) {
            return init;
        }

        if (Entities.isArthropod(entity) && item.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS)) {
            return init + 2.5 * item.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
        }

        if (Entities.isUndead(entity) && item.containsEnchantment(Enchantment.DAMAGE_UNDEAD)) {
            return init + 2.5 * item.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);
        }

        if (item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
            return init + (1.25 * item.getEnchantmentLevel(Enchantment.DAMAGE_ALL));
        }

        return init;
    }

    private double getDamageByArmor(ItemStack[] armor, double init) {
        double damage = init;
        double defensePoints = 0.0;

        for (ItemStack item : armor) {
            if (item == null) {
                continue;
            }

            double points = armorValues.getOrDefault(item.getType(), 0.0);

            if (item.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) {
                points += (item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) * 0.25);
            }

            defensePoints += points;
        }

        if (defensePoints == 0.0) {
            return damage;
        }

        damage = (damage / defensePoints);

        return damage;
    }
}