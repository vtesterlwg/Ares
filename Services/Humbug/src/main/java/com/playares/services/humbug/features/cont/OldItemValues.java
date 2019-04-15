package com.playares.services.humbug.features.cont;

import com.google.common.collect.ImmutableMap;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
            .put(Material.WOODEN_AXE, 2.4).put(Material.STONE_AXE, 2.6).put(Material.IRON_AXE, 2.8).put(Material.GOLDEN_AXE, 2.8).put(Material.DIAMOND_AXE, 3.0)
            .put(Material.WOODEN_SHOVEL, 1.0).put(Material.STONE_SHOVEL, 1.0).put(Material.IRON_SHOVEL, 1.0).put(Material.GOLDEN_SHOVEL, 1.0).put(Material.DIAMOND_SHOVEL, 1.0)
            .put(Material.WOODEN_PICKAXE, 1.0).put(Material.STONE_PICKAXE, 1.0).put(Material.IRON_PICKAXE, 1.0).put(Material.GOLDEN_PICKAXE, 1.0).put(Material.DIAMOND_PICKAXE, 1.0)
            .put(Material.WOODEN_HOE, 1.0).put(Material.STONE_HOE, 1.0).put(Material.IRON_HOE, 1.0).put(Material.GOLDEN_HOE, 1.0).put(Material.DIAMOND_HOE, 1.0)
            .put(Material.WOODEN_SWORD, 1.5).put(Material.STONE_SWORD, 1.5).put(Material.IRON_SWORD, 1.5).put(Material.GOLDEN_SWORD, 1.5).put(Material.DIAMOND_SWORD, 1.5)
            .put(Material.TRIDENT, 2.0)
            .build();

    public OldItemValues(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("old-item-values.enabled");
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

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getDamager();
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null) {
            return;
        }

        double damage = event.getDamage();
        damage = damage / weaponValues.getOrDefault(hand.getType(), 1.0);

        if (hand.containsEnchantment(Enchantment.DAMAGE_ALL)) {
            damage = damage * (((double)hand.getEnchantmentLevel(Enchantment.DAMAGE_ALL) / 2.0) + 1.0);
        }

        if (damage <= 0.0) {
            damage = 0.0;
        }

        event.setDamage(damage);
    }
}