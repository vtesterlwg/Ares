package com.playares.services.humbug.features.cont;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class KitLimits implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter public final Set<PotionLimit> potionLimits;
    @Getter public final Set<EnchantLimit> enchantLimits;

    @Getter public final ImmutableMap<PotionEffectType, Integer> extendedValues = ImmutableMap.<PotionEffectType, Integer>builder()
            .put(PotionEffectType.INVISIBILITY, (480 * 20)).put(PotionEffectType.NIGHT_VISION, (480 * 20)).put(PotionEffectType.JUMP, (480 * 20))
            .put(PotionEffectType.FIRE_RESISTANCE, (480 * 20)).put(PotionEffectType.SPEED, (480 * 20)).put(PotionEffectType.SLOW, (480 * 20))
            .put(PotionEffectType.WATER_BREATHING, (480 * 20)).put(PotionEffectType.POISON, (90 * 20)).put(PotionEffectType.REGENERATION, (90 * 20))
            .put(PotionEffectType.INCREASE_DAMAGE, (480 * 20)).put(PotionEffectType.WEAKNESS, (240 * 20))
            .build();

    @Getter @Setter public boolean enabled;

    public KitLimits(HumbugService humbug) {
        this.humbug = humbug;
        this.potionLimits = Sets.newHashSet();
        this.enchantLimits = Sets.newHashSet();
    }

    @Override
    public String getName() {
        return "Kit Limits";
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("kit-limits.enabled");

        for (String enchantName : humbug.getHumbugConfig().getConfigurationSection("kit-limits.enchantments").getKeys(false)) {
            final Enchantment enchantment = Enchantment.getByName(enchantName);

            if (enchantment == null) {
                Logger.error("Skipped enchantment '" + enchantName + "' - Enchantment not found");
                continue;
            }

            final boolean disabled = humbug.getHumbugConfig().getBoolean("kit-limits.enchantments." + enchantName + ".disabled");
            final int maxLevel = humbug.getHumbugConfig().getInt("kit-limits.enchantments." + enchantName + ".max-level");

            final EnchantLimit limit = new EnchantLimit(enchantment, disabled, maxLevel);
            enchantLimits.add(limit);
        }

        for (String potionName : humbug.getHumbugConfig().getConfigurationSection("kit-limits.potions").getKeys(false)) {
            final PotionEffectType potion = PotionEffectType.getByName(potionName);

            if (potion == null) {
                Logger.error("Skipped potion '" + potionName + "' - Potion not found");
                continue;
            }

            final boolean disabled = humbug.getHumbugConfig().getBoolean("kit-limits.potions." + potionName + ".disabled");
            final boolean amplifiable = humbug.getHumbugConfig().getBoolean("kit-limits.potions." + potionName + ".amplifiable");
            final boolean extendable = humbug.getHumbugConfig().getBoolean("kit-limits.potions." + potionName + ".extendable");

            final PotionLimit limit = new PotionLimit(potion, disabled, extendable, amplifiable);
            potionLimits.add(limit);
        }

        Logger.print("Loaded " + enchantLimits.size() + " Enchantment Limits");
        Logger.print("Loaded " + potionLimits.size() + " Potion Limits");
    }

    @Override
    public void start() {
        getHumbug().registerListener(this);
    }

    @Override
    public void stop() {
        PlayerDamagePlayerEvent.getHandlerList().unregister(this);
        PlayerItemConsumeEvent.getHandlerList().unregister(this);
        PrepareItemEnchantEvent.getHandlerList().unregister(this);
        PotionSplashEvent.getHandlerList().unregister(this);
    }

    public PotionLimit getPotionLimit(PotionEffectType type) {
        return potionLimits.stream().filter(p -> p.getType().equals(type)).findFirst().orElse(null);
    }

    public EnchantLimit getEnchantLimit(Enchantment type) {
        return enchantLimits.stream().filter(e -> e.getType().equals(type)).findFirst().orElse(null);
    }

    public void updateEnchantments(Player player, ItemStack item) {
        if (item.getEnchantments().isEmpty()) {
            return;
        }

        final List<Enchantment> toRemove = Lists.newArrayList();
        final Map<Enchantment, Integer> toLower = Maps.newHashMap();

        for (Enchantment enchantment : item.getEnchantments().keySet()) {
            final int level = item.getEnchantmentLevel(enchantment);
            final EnchantLimit limit = getEnchantLimit(enchantment);

            if (limit == null) {
                continue;
            }

            if (limit.isDisabled()) {
                toRemove.add(enchantment);
                continue;
            }

            if (limit.getMaxLevel() < level) {
                toLower.put(enchantment, limit.getMaxLevel());
            }
        }

        toRemove.forEach(removed -> {
            item.removeEnchantment(removed);

            player.sendMessage(ChatColor.DARK_RED + "Removed Enchantment" + ChatColor.RED + ": " +
                    ChatColor.WHITE + StringUtils.capitaliseAllWords(removed.getName().toLowerCase().replace("_", " ")));
        });

        toLower.keySet().forEach(lowered -> {
            final int level = toLower.get(lowered);
            item.addUnsafeEnchantment(lowered, level);

            player.sendMessage(ChatColor.BLUE + "Updated Enchantment" + ChatColor.AQUA + ": " +
                    ChatColor.WHITE + StringUtils.capitaliseAllWords(lowered.getName().toLowerCase().replace("_", " ")));
        });
    }

    @EventHandler
    public void onEnchant(PrepareItemEnchantEvent event) {
        if (!isEnabled()) {
            return;
        }

        for (EnchantmentOffer offer : event.getOffers()) {
            final EnchantLimit limit = getEnchantLimit(offer.getEnchantment());

            if (limit == null) {
                continue;
            }

            if (limit.isDisabled()) {
                continue;
            }

            if (limit.getMaxLevel() < offer.getEnchantmentLevel()) {
                offer.setEnchantmentLevel(limit.getMaxLevel());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            final Player player = (Player)event.getEntity();

            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor == null || armor.getType().equals(Material.AIR)) {
                    continue;
                }

                updateEnchantments(player, armor);
            }
        }

        if (event.getDamager() instanceof Player) {
            final Player player = (Player)event.getDamager();
            final ItemStack item = player.getInventory().getItemInMainHand();

            if (item == null || item.getType().equals(Material.AIR)) {
                return;
            }

            updateEnchantments(player, item);
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final ItemStack item = event.getBow();

        if (item == null || item.getType().equals(Material.AIR)) {
            return;
        }

        updateEnchantments(player, item);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!isEnabled()) {
            return;
        }

        for (PotionEffect effect : event.getPotion().getEffects()) {
            final PotionLimit limit = getPotionLimit(effect.getType());

            if (limit == null) {
                continue;
            }

            if (limit.isDisabled()) {
                event.setCancelled(true);
                event.getAffectedEntities().clear();
                return;
            }

            if (!limit.isAmplifiable() && effect.getAmplifier() > 0) {
                event.setCancelled(true);
                event.getAffectedEntities().clear();
                return;
            }

            if (!limit.isExtendable()) {
                final int durationLimit = extendedValues.getOrDefault(effect.getType(), Integer.MAX_VALUE);

                if (effect.getDuration() >= durationLimit) {
                    event.setCancelled(true);
                    event.getAffectedEntities().clear();
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        if (!isEnabled()) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item == null || !item.getType().equals(Material.POTION)) {
            return;
        }

        final PotionMeta meta = (PotionMeta)item.getItemMeta();
        final PotionLimit limit = getPotionLimit(meta.getBasePotionData().getType().getEffectType());

        if (limit == null) {
            return;
        }

        if (limit.isDisabled()) {
            event.setCancelled(true);
            return;
        }

        if (!limit.isAmplifiable() && meta.getBasePotionData().isUpgraded()) {
            event.setCancelled(true);
            return;
        }

        if (!limit.isExtendable() && meta.getBasePotionData().isExtended()) {
            event.setCancelled(true);
        }
    }

    @AllArgsConstructor
    public final class EnchantLimit {
        @Getter public final Enchantment type;
        @Getter public final boolean disabled;
        @Getter public final int maxLevel;
    }

    @AllArgsConstructor
    public final class PotionLimit {
        @Getter public final PotionEffectType type;
        @Getter public final boolean disabled;
        @Getter public final boolean extendable;
        @Getter public final boolean amplifiable;
    }
}
