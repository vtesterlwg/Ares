package com.riotmc.services.humbug.features.cont;

import com.playares.commons.bukkit.util.Scheduler;
import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class OldPotions implements HumbugModule, Listener {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean oldHealthEnabled;

    @Getter @Setter
    public boolean oldStrengthEnabled;

    @Getter @Setter
    public boolean oldRegenEnabled;

    @Getter @Setter
    public boolean enabled;

    public OldPotions(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("oldpotions.enabled");
        this.oldHealthEnabled = humbug.getHumbugConfig().getBoolean("oldpotions.old-health-enabled");
        this.oldStrengthEnabled = humbug.getHumbugConfig().getBoolean("oldpotions.old-strength-enabled");
        this.oldRegenEnabled = humbug.getHumbugConfig().getBoolean("oldpotions.old-regen-enabled");
    }

    public String getName() {
        return "1.5 Potion Values";
    }

    public void start() {
        getHumbug().getOwner().registerListener(this);
    }

    public void stop() {
        EntityRegainHealthEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    private double calculateFinalDamage(Player player, double init) {
        double damage = init;

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                damage += (effect.getAmplifier() * 1.3);
                continue;
            }

            if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
                damage += (effect.getAmplifier() * -0.5);
            }
        }

        return damage;
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (!isEnabled() || !isOldStrengthEnabled()) {
            return;
        }

        final Entity damager = event.getDamager();

        if (!(damager instanceof Player)) {
            return;
        }

        event.setDamage(calculateFinalDamage((Player)damager, event.getDamage()));
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!isEnabled() || (!isOldRegenEnabled() && !isOldHealthEnabled())) {
            return;
        }

        final LivingEntity entity = (LivingEntity)event.getEntity();
        int level = 0;

        for (PotionEffect effect : entity.getActivePotionEffects()) {
            final PotionEffectType type = effect.getType();
            final int amplifier = effect.getAmplifier();

            if (type.equals(PotionEffectType.REGENERATION) || type.equals(PotionEffectType.HEAL)) {
                level = amplifier + 1;
                break;
            }
        }

        final EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();
        final double amount = event.getAmount();

        if (isOldHealthEnabled() && reason.equals(EntityRegainHealthEvent.RegainReason.MAGIC) && amount > 1.0 && level >= 0) {
            event.setAmount(amount * 1.5);
            return;
        }

        if (isOldRegenEnabled() && reason.equals(EntityRegainHealthEvent.RegainReason.MAGIC_REGEN) && amount == 1.0 && level > 0) {
            new Scheduler(getHumbug().getOwner()).sync(() -> {
                if (entity.isDead()) {
                    return;
                }

                final double max = entity.getMaxHealth();
                final double current = entity.getHealth();

                if (max >= current) {
                    return;
                }

                entity.setHealth((max >= current + 1.0) ? current + 1.0 : max);
            }).delay(50L / (level * 2)).run();
        }
    }
}