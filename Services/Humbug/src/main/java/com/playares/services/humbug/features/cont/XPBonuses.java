package com.playares.services.humbug.features.cont;

import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public final class XPBonuses implements HumbugModule, Listener {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean enabled;

    @Getter @Setter
    public double lootingMultiplier;

    @Getter @Setter
    public double fortuneMutliplier;

    @Getter @Setter
    public double bottleMultiplier;

    public XPBonuses(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("xp-bonuses.enabled");
        this.lootingMultiplier = humbug.getHumbugConfig().getDouble("xp-bonuses.multipliers.looting");
        this.fortuneMutliplier = humbug.getHumbugConfig().getDouble("xp-bonuses.multipliers.fortune");
        this.bottleMultiplier = humbug.getHumbugConfig().getDouble("xp-bonuses.multipliers.bottle");
    }

    @Override
    public String getName() {
        return "XP Bonuses";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);
    }

    @Override
    public void stop() {
        BlockBreakEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
        ExpBottleEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null || !hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
            return;
        }

        final int enchantMultiplier = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1;
        final int xp = event.getExpToDrop();

        event.setExpToDrop((int)(Math.round(xp * (enchantMultiplier * fortuneMutliplier))));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isEnabled()) {
            return;
        }

        final LivingEntity entity = event.getEntity();
        final Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        final ItemStack hand = killer.getInventory().getItemInMainHand();

        if (hand == null || !hand.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
            return;
        }

        final int enchantMultiplier = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) + 1;
        final int xp = event.getDroppedExp();

        event.setDroppedExp((int)(Math.round(xp * (enchantMultiplier * lootingMultiplier))));
    }

    @EventHandler
    public void onBottleSplash(ExpBottleEvent event) {
        if (!isEnabled()) {
            return;
        }

        final ThrownExpBottle entity = event.getEntity();
        final ProjectileSource shooter = entity.getShooter();

        if (!(shooter instanceof Player)) {
            return;
        }

        final Player player = (Player)shooter;
        final int xp = event.getExperience();

        event.setShowEffect(false);
        event.setExperience(0);

        player.giveExp((int)(Math.round(xp * bottleMultiplier)));
    }
}
